package com.todayit.course.service;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.Theme;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.InvalidCoursePlaceOrderException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.repository.CourseRepository;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CreateCourseResult;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 코스의 생성 조건과 분류 관계를 저장합니다. */
@Service
public class CourseCommandService {

  private final CourseRepository courseRepository;

  /**
   * 코스 저장소를 받습니다.
   *
   * @param courseRepository 코스 저장소
   */
  public CourseCommandService(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  /**
   * 입력 조건과 분류 관계를 함께 저장합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 검증된 생성 조건
   * @return 생성 결과
   * @throws CourseException 지원하지 않는 콘셉트가 포함된 경우
   */
  @Transactional
  public CreateCourseResult createCourse(String memberId, CreateCourseCommand command) {
    List<Theme> themes = courseRepository.findThemesByNames(command.themes());
    if (themes.size() != command.themes().size()) {
      throw new CourseException(CourseErrorCode.UNSUPPORTED_THEME);
    }
    Course course =
        Course.createDraft(
            memberId,
            command.title(),
            command.transport(),
            command.startAt(),
            command.endAt(),
            themes,
            LocalDateTime.now());
    return CreateCourseResult.from(courseRepository.save(course));
  }

  /**
   * 코스 행을 잠근 뒤 새 장소를 추가하고 저장합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 코스 장소 추가 조건
   * @return 추가 결과
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws MaxPlaceDuplicationExceededException 동일한 장소가 이미 최대 허용 횟수(2개)만큼 존재하는 경우
   */
  @Transactional
  public AddCoursePlaceResult addPlace(String memberId, AddCoursePlaceCommand command) {
    Course course =
        courseRepository
            .findByIdAndMemberIdForUpdate(command.courseId(), memberId)
            .orElseThrow(CourseNotFoundException::new);
    if (!course.isWriter(memberId)
        || course.getDeletedAt() != null
        || course.getStatus() == CourseStatus.DELETED) {
      throw new CourseNotFoundException();
    }
    if (!course.canAddPlace(command.placeId())) {
      throw new MaxPlaceDuplicationExceededException();
    }
    if (command.order() != null) {
      List<CoursePlace> shifted = course.shiftOrdersToNegative(command.order());
      if (!shifted.isEmpty()) {
        courseRepository.flush();
        course.restoreShiftedOrders(shifted);
        courseRepository.flush();
      }
    }
    CoursePlace coursePlace =
        course.addPlace(command.placeId(), command.order(), command.stayMinute());
    Course savedCourse = courseRepository.saveAndFlush(course);
    CoursePlace persistedPlace =
        (savedCourse != null)
            ? savedCourse.getCoursePlaces().stream()
                .filter(cp -> cp.getOrder() == coursePlace.getOrder())
                .findFirst()
                .orElse(coursePlace)
            : coursePlace;
    return AddCoursePlaceResult.from(persistedPlace);
  }

  /**
   * 코스 행을 잠근 뒤 장소의 방문 순서를 일괄 변경합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 순서 변경 조건
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws InvalidCoursePlaceOrderException 장소 ID 목록이 null이거나 비어 있거나 현재 코스의 장소 ID 목록과 일치하지 않는 경우
   */
  @Transactional
  public void reorderPlaces(String memberId, UpdateCoursePlaceOrdersCommand command) {
    Course course =
        courseRepository
            .findByIdAndMemberIdForUpdate(command.courseId(), memberId)
            .orElseThrow(CourseNotFoundException::new);
    if (!course.isWriter(memberId)
        || course.getDeletedAt() != null
        || course.getStatus() == CourseStatus.DELETED) {
      throw new CourseNotFoundException();
    }
    course.reorderPlaces(command.coursePlaceIds());
    courseRepository.flush();
    course.applyNewOrders(command.coursePlaceIds());
    courseRepository.saveAndFlush(course);
  }
}

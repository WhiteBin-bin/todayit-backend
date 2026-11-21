package com.todayit.course.service;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.Theme;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.CoursePlaceNotFoundException;
import com.todayit.course.exception.InvalidCoursePlaceOrderException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.repository.CourseRepository;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceEntities;
import com.todayit.course.service.model.CreateCourseResult;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 코스의 생성 조건과 분류 관계를 저장합니다. */
@Service
public class CourseCommandService {

  /** 한 코스 내 동일한 장소의 최대 중복 허용 개수입니다. */
  public static final int MAX_PLACE_DUPLICATION_COUNT = 2;

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
   * @return 추가된 장소 및 전체 장소 엔티티 묶음
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws MaxPlaceDuplicationExceededException 이미 동일 장소가 최대 중복 개수(2개)만큼 존재하는 경우
   */
  @Transactional
  public AddCoursePlaceEntities addPlace(String memberId, AddCoursePlaceCommand command) {
    Course course =
        courseRepository
            .findByIdAndMemberIdForUpdate(command.courseId(), memberId)
            .orElseThrow(CourseNotFoundException::new);
    if (!course.getMemberId().equals(memberId)
        || course.getDeletedAt() != null
        || course.getStatus() == CourseStatus.DELETED) {
      throw new CourseNotFoundException();
    }

    if (command.order() != null) {
      List<CoursePlace> shiftedPlaces = shiftOrdersToNegative(course, command.order());
      if (!shiftedPlaces.isEmpty()) {
        courseRepository.flush();
        restoreShiftedOrders(shiftedPlaces);
      }
    }

    CoursePlace coursePlace =
        addPlace(course, command.placeId(), command.order(), command.stayMinute());
    Course savedCourse = courseRepository.saveAndFlush(course);
    CoursePlace persistedPlace =
        savedCourse.getCoursePlaces().stream()
            .filter(
                cp ->
                    cp.getPlaceId() == coursePlace.getPlaceId()
                        && cp.getOrder() == coursePlace.getOrder())
            .findFirst()
            .orElse(coursePlace);

    return new AddCoursePlaceEntities(persistedPlace, savedCourse.getCoursePlaces());
  }

  /**
   * 코스 행을 잠근 뒤 장소들의 방문 순서를 일괄 변경합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 순서 변경 조건
   * @return 변경된 전체 코스 장소 목록
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws InvalidCoursePlaceOrderException 전달된 장소 목록이 유효하지 않은 경우
   */
  @Transactional
  public List<CoursePlace> updatePlaceOrders(
      String memberId, UpdateCoursePlaceOrdersCommand command) {
    Course course =
        courseRepository
            .findByIdAndMemberIdForUpdate(command.courseId(), memberId)
            .orElseThrow(CourseNotFoundException::new);
    if (!course.getMemberId().equals(memberId)
        || course.getDeletedAt() != null
        || course.getStatus() == CourseStatus.DELETED) {
      throw new CourseNotFoundException();
    }
    reorderPlaces(course, command.coursePlaceIds());
    courseRepository.flush();
    confirmReorder(course);
    Course savedCourse = courseRepository.saveAndFlush(course);
    return savedCourse.getCoursePlaces();
  }

  /**
   * 코스 행을 잠근 뒤 특정 장소를 삭제하고 남은 장소들의 순서를 재조정합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param coursePlaceId 삭제할 코스 장소 식별자
   * @return 삭제 후 재정렬된 남은 코스 장소 목록
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우, 또는 이미 삭제된 코스인 경우
   * @throws CoursePlaceNotFoundException 코스 내에 해당 장소가 없는 경우
   */
  @Transactional
  public List<CoursePlace> removePlace(String memberId, int courseId, int coursePlaceId) {
    Course course =
        courseRepository
            .findByIdAndMemberIdForUpdate(courseId, memberId)
            .orElseThrow(CourseNotFoundException::new);
    if (!course.getMemberId().equals(memberId)
        || course.getDeletedAt() != null
        || course.getStatus() == CourseStatus.DELETED) {
      throw new CourseNotFoundException();
    }
    CoursePlace targetPlace =
        course.getCoursePlaces().stream()
            .filter(cp -> cp.getCoursePlaceId() == coursePlaceId)
            .findFirst()
            .orElseThrow(CoursePlaceNotFoundException::new);

    List<CoursePlace> subsequentPlaces = shiftOrdersToNegativeForRemoval(course, targetPlace);
    course.getCoursePlaces().remove(targetPlace);
    if (!subsequentPlaces.isEmpty()) {
      courseRepository.flush();
      restoreShiftedOrdersForRemoval(subsequentPlaces);
    }
    Course savedCourse = courseRepository.saveAndFlush(course);
    return savedCourse.getCoursePlaces();
  }

  /**
   * 코스 행을 잠근 뒤 작성자 확인 후 코스와 관련 연관 데이터를 데이터베이스에서 영구 삭제합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 삭제할 코스 식별자
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우, 또는 이미 삭제된 코스인 경우
   */
  @Transactional
  public void deleteCourse(String memberId, int courseId) {
    Course course =
        courseRepository
            .findByIdAndMemberIdForUpdate(courseId, memberId)
            .orElseThrow(CourseNotFoundException::new);
    if (!course.getMemberId().equals(memberId)
        || course.getDeletedAt() != null
        || course.getStatus() == CourseStatus.DELETED) {
      throw new CourseNotFoundException();
    }
    courseRepository.delete(course);
    courseRepository.flush();
  }

  /**
   * 확정된 원본 코스를 복사하여 새로운 DRAFT 코스를 생성합니다.
   *
   * @param memberId 인증된 요청 회원 식별자
   * @param courseId 복사할 원본 코스 식별자
   * @return 생성된 DRAFT 코스 엔티티
   * @throws CourseNotFoundException 코스가 없거나 삭제되었거나 타인의 비공개 코스인 경우
   * @throws CourseException DRAFT 상태의 코스를 복사하려는 경우
   */
  @Transactional
  public Course copyCourse(String memberId, int courseId) {
    Course sourceCourse =
        courseRepository
            .findByIdWithPlaces(courseId)
            .filter(c -> c.getDeletedAt() == null && c.getStatus() != CourseStatus.DELETED)
            .orElseThrow(CourseNotFoundException::new);

    // 타인의 비공개 코스는 존재 여부를 노출하지 않기 위해 404 처리
    if (sourceCourse.getVisibility() == CourseVisibility.PRIVATE
        && !sourceCourse.getMemberId().equals(memberId)) {
      throw new CourseNotFoundException();
    }

    // DRAFT 상태의 코스는 복사 대상에서 제외 (본인 DRAFT 포함)
    if (sourceCourse.getStatus() == CourseStatus.DRAFT) {
      throw new CourseException(CourseErrorCode.CANNOT_COPY_DRAFT);
    }

    if (!canCopy(sourceCourse, memberId)) {
      throw new CourseNotFoundException();
    }

    Course copiedDraft = Course.createCopyDraft(memberId, sourceCourse, LocalDateTime.now());
    return courseRepository.save(copiedDraft);
  }

  /**
   * 특정 회원이 대상 코스를 복사할 수 있는지 검증합니다.
   *
   * @param course 검증할 코스
   * @param memberId 요청 회원 식별자
   * @return 복사 가능 여부
   */
  private boolean canCopy(Course course, String memberId) {
    if (course.getDeletedAt() != null || course.getStatus() != CourseStatus.ACTIVE) {
      return false;
    }
    if (course.getVisibility() == CourseVisibility.PUBLIC) {
      return true;
    }
    return course.getMemberId().equals(memberId);
  }

  /**
   * 코스에 새 장소를 검증 후 추가합니다.
   *
   * @param course 대상 코스 엔티티
   * @param placeId 추가할 장소 식별자
   * @param targetOrder 지정할 방문 순서, null이면 맨 뒤에 추가
   * @param stayMinute 체류 시간(분), null 허용
   * @return 생성된 CoursePlace 엔티티
   * @throws MaxPlaceDuplicationExceededException 동일 장소가 2개를 초과하는 경우
   */
  private CoursePlace addPlace(
      Course course, int placeId, Integer targetOrder, Integer stayMinute) {
    long placeCount =
        course.getCoursePlaces().stream().filter(cp -> cp.getPlaceId() == placeId).count();
    if (placeCount >= MAX_PLACE_DUPLICATION_COUNT) {
      throw new MaxPlaceDuplicationExceededException();
    }
    int maxOrder =
        course.getCoursePlaces().stream().mapToInt(CoursePlace::getOrder).max().orElse(0);
    int assignedOrder =
        (targetOrder == null || targetOrder > maxOrder) ? maxOrder + 1 : Math.max(1, targetOrder);
    CoursePlace newCoursePlace = CoursePlace.create(course, placeId, assignedOrder, stayMinute);
    course.getCoursePlaces().add(newCoursePlace);
    return newCoursePlace;
  }

  /**
   * 코스 장소들의 순서를 새로운 순서 목록대로 임시 음수 순서로 설정합니다.
   *
   * @param course 대상 코스 엔티티
   * @param newOrderPlaceIds 새로운 방문 순서대로 나열된 코스 장소 식별자 목록
   * @throws InvalidCoursePlaceOrderException 목록이 비어 있거나 기존 장소 목록과 일치하지 않는 경우
   */
  private void reorderPlaces(Course course, List<Integer> newOrderPlaceIds) {
    List<CoursePlace> places = course.getCoursePlaces();
    if (newOrderPlaceIds == null
        || newOrderPlaceIds.isEmpty()
        || newOrderPlaceIds.size() != places.size()) {
      throw new InvalidCoursePlaceOrderException();
    }

    Set<Integer> currentPlaceIds =
        places.stream().map(CoursePlace::getCoursePlaceId).collect(Collectors.toSet());
    Set<Integer> requestedPlaceIds = new HashSet<>(newOrderPlaceIds);

    if (!currentPlaceIds.equals(requestedPlaceIds)) {
      throw new InvalidCoursePlaceOrderException();
    }

    Map<Integer, CoursePlace> placeMap =
        places.stream()
            .collect(Collectors.toMap(CoursePlace::getCoursePlaceId, Function.identity()));

    for (int i = 0; i < newOrderPlaceIds.size(); i++) {
      CoursePlace place = placeMap.get(newOrderPlaceIds.get(i));
      place.setOrder(-(i + 1));
    }
  }

  /**
   * 임시 음수 순서로 설정된 장소들을 양수 순서로 반전하여 확정합니다.
   *
   * @param course 대상 코스 엔티티
   */
  private void confirmReorder(Course course) {
    for (CoursePlace place : course.getCoursePlaces()) {
      if (place.getOrder() < 0) {
        place.setOrder(-place.getOrder());
      }
    }
  }

  /**
   * 새 장소를 특정 순서에 삽입하기 위해 기존 장소들의 순서를 음수 임시 순서로 변경합니다.
   *
   * @param course 대상 코스 엔티티
   * @param targetOrder 새로 추가할 장소의 목표 순서
   * @return 임시 음수 순서로 변경된 장소 목록
   */
  private List<CoursePlace> shiftOrdersToNegative(Course course, int targetOrder) {
    int maxOrder =
        course.getCoursePlaces().stream().mapToInt(CoursePlace::getOrder).max().orElse(0);
    if (targetOrder > maxOrder) {
      return List.of();
    }
    int assignedOrder = Math.max(1, targetOrder);
    List<CoursePlace> targets =
        course.getCoursePlaces().stream().filter(cp -> cp.getOrder() >= assignedOrder).toList();
    for (CoursePlace cp : targets) {
      cp.setOrder(-cp.getOrder());
    }
    return targets;
  }

  /**
   * 음수 임시 순서로 변경된 장소들의 순서를 1씩 뒤로 밀린 양수 순서로 복원합니다.
   *
   * @param shiftedPlaces 임시 음수 순서로 변경된 장소 목록
   */
  private void restoreShiftedOrders(List<CoursePlace> shiftedPlaces) {
    for (CoursePlace cp : shiftedPlaces) {
      cp.setOrder(-cp.getOrder() + 1);
    }
  }

  /**
   * 장소 삭제 시 삭제 대상 장소의 뒤에 위치한 장소들을 임시 음수 순서로 변경합니다.
   *
   * @param course 대상 코스 엔티티
   * @param targetPlace 삭제 대상 장소 엔티티
   * @return 임시 음수 순서로 변경된 장소 목록
   */
  private List<CoursePlace> shiftOrdersToNegativeForRemoval(
      Course course, CoursePlace targetPlace) {
    List<CoursePlace> subsequentPlaces =
        course.getCoursePlaces().stream()
            .filter(cp -> cp.getOrder() > targetPlace.getOrder())
            .toList();

    for (CoursePlace cp : subsequentPlaces) {
      cp.setOrder(-cp.getOrder());
    }
    return subsequentPlaces;
  }

  /**
   * 음수 임시 순서로 변경된 장소들의 순서를 1씩 앞당긴 양수 순서로 복원합니다.
   *
   * @param shiftedPlaces 임시 음수 순서로 변경된 장소 목록
   */
  private void restoreShiftedOrdersForRemoval(List<CoursePlace> shiftedPlaces) {
    for (CoursePlace cp : shiftedPlaces) {
      cp.setOrder(-cp.getOrder() - 1);
    }
  }
}

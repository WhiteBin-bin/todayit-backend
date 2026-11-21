package com.todayit.course.service;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.RegionCourse;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.CoursePlaceNotFoundException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.exception.PlaceNotFoundException;
import com.todayit.course.repository.CourseMemberLikeRepository;
import com.todayit.course.repository.CourseScrapRepository;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceEntities;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CopyCourseResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CoursePlaceDetailResult;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CourseRegionResult;
import com.todayit.course.service.model.CreateCourseResult;
import com.todayit.course.service.model.RemoveCoursePlaceResult;
import com.todayit.course.service.model.UpdateCoursePlaceOrdersResult;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 행정 지역 검증과 코스 생성 및 조회를 조정합니다. */
@Service
public class CourseFacade {

  private final RegionQueryService regionQueryService;
  private final CourseCommandService courseCommandService;
  private final CourseQueryService courseQueryService;
  private final PlaceCourseQueryService placeCourseQueryService;
  private final CourseMemberLikeRepository courseMemberLikeRepository;
  private final CourseScrapRepository courseScrapRepository;

  /**
   * 지역 조회, 코스 저장/조회, 장소 조회 및 반응 저장소 계약을 받습니다.
   *
   * @param regionQueryService 지역 검증 서비스
   * @param courseCommandService 코스 저장 서비스
   * @param courseQueryService 코스 조회 서비스
   * @param placeCourseQueryService 장소 조회 서비스
   * @param courseMemberLikeRepository 코스 좋아요 저장소
   * @param courseScrapRepository 코스 스크랩 저장소
   */
  public CourseFacade(
      RegionQueryService regionQueryService,
      CourseCommandService courseCommandService,
      CourseQueryService courseQueryService,
      PlaceCourseQueryService placeCourseQueryService,
      CourseMemberLikeRepository courseMemberLikeRepository,
      CourseScrapRepository courseScrapRepository) {
    this.regionQueryService = regionQueryService;
    this.courseCommandService = courseCommandService;
    this.courseQueryService = courseQueryService;
    this.placeCourseQueryService = placeCourseQueryService;
    this.courseMemberLikeRepository = courseMemberLikeRepository;
    this.courseScrapRepository = courseScrapRepository;
  }

  /**
   * 지역과 코스 엔티티를 생성하고 관계를 연결합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 검증된 생성 조건
   * @return 생성 결과
   */
  @Transactional
  public CreateCourseResult createCourse(String memberId, CreateCourseCommand command) {
    return courseCommandService.createCourse(memberId, command);
  }

  /**
   * 코스에 장소를 추가하고 추가된 장소 상세 및 정렬된 전체 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 장소 추가 조건
   * @return 추가된 장소 상세 및 전체 정렬된 장소 목록
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws PlaceNotFoundException 장소가 없거나 삭제된 경우
   * @throws MaxPlaceDuplicationExceededException 코스 내 동일 장소가 이미 2개인 경우
   */
  @Transactional
  public AddCoursePlaceResult addPlaceToCourse(String memberId, AddCoursePlaceCommand command) {
    if (!placeCourseQueryService.existsActivePlace(command.placeId())) {
      throw new PlaceNotFoundException();
    }
    AddCoursePlaceEntities entities = courseCommandService.addPlace(memberId, command);
    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(entities.places());
    CoursePlaceDetailResult addedPlace =
        places.stream()
            .filter(p -> p.coursePlaceId() == entities.addedPlace().getCoursePlaceId())
            .findFirst()
            .orElseGet(
                () -> {
                  Map<Integer, CoursePlaceItem> placeMap =
                      placeCourseQueryService.findPlacesByIds(
                          List.of(entities.addedPlace().getPlaceId()));
                  CoursePlaceItem item = placeMap.get(entities.addedPlace().getPlaceId());
                  return new CoursePlaceDetailResult(
                      entities.addedPlace().getCoursePlaceId(),
                      entities.addedPlace().getPlaceId(),
                      item != null ? item.name() : "",
                      item != null ? item.category() : "",
                      item != null ? item.address() : "",
                      item != null ? item.latitude() : BigDecimal.ZERO,
                      item != null ? item.longitude() : BigDecimal.ZERO,
                      item != null ? item.imageUrl() : null,
                      entities.addedPlace().getOrder(),
                      entities.addedPlace().getStayMinute());
                });
    return new AddCoursePlaceResult(addedPlace, places);
  }

  /**
   * 코스에 포함된 장소들의 방문 순서를 일괄 재조정하고 변경된 전체 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 순서 변경 명령
   * @return 순서가 재조정된 전체 장소 상세 목록
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   */
  @Transactional
  public UpdateCoursePlaceOrdersResult updateCoursePlaceOrders(
      String memberId, UpdateCoursePlaceOrdersCommand command) {
    List<CoursePlace> updatedPlaces = courseCommandService.updatePlaceOrders(memberId, command);
    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(updatedPlaces);
    return new UpdateCoursePlaceOrdersResult(places);
  }

  /**
   * 코스에서 특정 장소를 삭제하고 후속 장소 순서를 앞당긴 후, 삭제된 장소 ID와 남은 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param coursePlaceId 삭제할 코스 장소 식별자
   * @return 삭제된 장소 ID 및 순서가 재조정된 남은 장소 목록
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws CoursePlaceNotFoundException 해당 코스 장소가 존재하지 않는 경우
   */
  @Transactional
  public RemoveCoursePlaceResult removePlaceFromCourse(
      String memberId, int courseId, int coursePlaceId) {
    List<CoursePlace> remainingPlaces =
        courseCommandService.removePlace(memberId, courseId, coursePlaceId);
    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(remainingPlaces);
    return new RemoveCoursePlaceResult(coursePlaceId, places);
  }

  /**
   * 코스를 물리적으로 완전히 삭제합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 삭제할 코스 식별자
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   */
  @Transactional
  public void deleteCourse(String memberId, int courseId) {
    courseCommandService.deleteCourse(memberId, courseId);
  }

  /**
   * 다른 회원의 공개 코스 또는 본인의 코스를 복사하여 새로운 DRAFT 코스를 생성합니다.
   *
   * @param memberId 복사를 요청한 회원 식별자
   * @param courseId 복사할 원본 코스 식별자
   * @return 복제된 코스 결과
   * @throws CourseNotFoundException 코스가 없거나 삭제되었거나 타인의 비공개 코스인 경우
   */
  @Transactional
  public CopyCourseResult copyCourse(String memberId, int courseId) {
    Course copiedCourse = courseCommandService.copyCourse(memberId, courseId);
    List<CoursePlaceDetailResult> places =
        toCoursePlaceDetailResults(copiedCourse.getCoursePlaces());
    return new CopyCourseResult(
        copiedCourse.getCourseId(),
        courseId,
        copiedCourse.getTitle(),
        copiedCourse.getStatus(),
        places,
        copiedCourse.getCreatedAt());
  }

  /**
   * 코스 식별자로 코스 상세 정보를 조회합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 요청 회원 식별자, 비회원이면 null
   * @return 코스 상세 정보
   * @throws CourseNotFoundException 코스가 없거나 비공개 코스에 접근할 권한이 없는 경우
   */
  @Transactional(readOnly = true)
  public CourseDetailResult getCourseDetail(int courseId, String memberId) {
    Course course = courseQueryService.getCourse(courseId);
    if (!canAccess(course, memberId)) {
      throw new CourseNotFoundException();
    }

    List<String> themeNames =
        course.getThemes().stream().map(ct -> ct.getTheme().getName()).toList();

    List<Integer> regionIds = course.getRegions().stream().map(RegionCourse::getRegionId).toList();
    CourseRegionResult regionResult = regionQueryService.getCourseRegionSummary(regionIds);

    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(course.getCoursePlaces());

    boolean liked =
        memberId != null
            && courseMemberLikeRepository.existsByMemberIdAndCourseCourseId(memberId, courseId);
    boolean scrapped =
        memberId != null
            && courseScrapRepository.existsByMemberIdAndCourseCourseIdAndActiveTrueAndDeletedFalse(
                memberId, courseId);

    return new CourseDetailResult(
        course.getCourseId(),
        course.getTitle(),
        course.getCourseSummary(),
        course.getTransport(),
        course.getStartAt(),
        course.getEndAt(),
        course.getStatus(),
        course.getVisibility(),
        course.getViewCount(),
        regionResult != null ? regionResult.sidoCode() : null,
        regionResult != null ? regionResult.sidoName() : null,
        regionResult != null ? regionResult.sigunguCode() : null,
        regionResult != null ? regionResult.sigunguName() : null,
        themeNames,
        memberId != null && course.getMemberId().equals(memberId),
        places,
        course.getCreatedAt(),
        course.getUpdatedAt(),
        liked,
        scrapped,
        course.getBeforeVisit());
  }

  private List<CoursePlaceDetailResult> toCoursePlaceDetailResults(List<CoursePlace> coursePlaces) {
    if (coursePlaces == null || coursePlaces.isEmpty()) {
      return List.of();
    }
    List<CoursePlace> sortedCoursePlaces =
        coursePlaces.stream().sorted(Comparator.comparingInt(CoursePlace::getOrder)).toList();
    List<Integer> placeIds = sortedCoursePlaces.stream().map(CoursePlace::getPlaceId).toList();
    Map<Integer, CoursePlaceItem> placeMap = placeCourseQueryService.findPlacesByIds(placeIds);

    return sortedCoursePlaces.stream()
        .map(
            cp -> {
              CoursePlaceItem item = placeMap.get(cp.getPlaceId());
              String name = item != null ? item.name() : "";
              String category = item != null ? item.category() : "";
              String address = item != null ? item.address() : "";
              BigDecimal latitude = item != null ? item.latitude() : BigDecimal.ZERO;
              BigDecimal longitude = item != null ? item.longitude() : BigDecimal.ZERO;
              String imageUrl = item != null ? item.imageUrl() : null;
              return new CoursePlaceDetailResult(
                  cp.getCoursePlaceId(),
                  cp.getPlaceId(),
                  name,
                  category,
                  address,
                  latitude,
                  longitude,
                  imageUrl,
                  cp.getOrder(),
                  cp.getStayMinute());
            })
        .toList();
  }

  private boolean canAccess(Course course, String memberId) {
    if (course.getDeletedAt() != null || course.getStatus() == CourseStatus.DELETED) {
      return false;
    }
    if (course.getVisibility() == CourseVisibility.PUBLIC
        && course.getStatus() == CourseStatus.ACTIVE) {
      return true;
    }
    return memberId != null && course.getMemberId().equals(memberId);
  }
}

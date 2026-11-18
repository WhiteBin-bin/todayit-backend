package com.todayit.course.service;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.RegionCourse;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.exception.PlaceNotFoundException;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CoursePlaceDetailResult;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CourseRegionResult;
import com.todayit.course.service.model.CreateCourseResult;
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

  /**
   * 지역 조회, 코스 저장/조회, 장소 조회 계약을 받습니다.
   *
   * @param regionQueryService 지역 검증 서비스
   * @param courseCommandService 코스 저장 서비스
   * @param courseQueryService 코스 조회 서비스
   * @param placeCourseQueryService 장소 조회 서비스 계약
   */
  public CourseFacade(
      RegionQueryService regionQueryService,
      CourseCommandService courseCommandService,
      CourseQueryService courseQueryService,
      PlaceCourseQueryService placeCourseQueryService) {
    this.regionQueryService = regionQueryService;
    this.courseCommandService = courseCommandService;
    this.courseQueryService = courseQueryService;
    this.placeCourseQueryService = placeCourseQueryService;
  }

  /**
   * 비공개 DRAFT 코스를 생성합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 코스 생성 조건
   * @return 생성 결과
   */
  public CreateCourseResult createCourse(String memberId, CreateCourseCommand command) {
    return courseCommandService.createCourse(memberId, command);
  }

  /**
   * 코스에 새 장소를 추가합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 코스 장소 추가 조건
   * @return 추가 결과
   * @throws CourseNotFoundException 코스가 없거나 작성자가 아닌 경우
   * @throws PlaceNotFoundException 장소가 존재하지 않거나 비활성화/삭제된 경우
   * @throws MaxPlaceDuplicationExceededException 동일한 장소가 이미 최대 허용 횟수(2개)만큼 존재하는 경우
   */
  @Transactional
  public AddCoursePlaceResult addPlaceToCourse(String memberId, AddCoursePlaceCommand command) {
    if (!placeCourseQueryService.existsActivePlace(command.placeId())) {
      throw new PlaceNotFoundException();
    }
    return courseCommandService.addPlace(memberId, command);
  }

  /**
   * 코스 장소의 방문 순서를 일괄 변경합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 순서 변경 조건
   */
  @Transactional
  public void updateCoursePlaceOrders(String memberId, UpdateCoursePlaceOrdersCommand command) {
    courseCommandService.reorderPlaces(memberId, command);
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
    if (!course.canAccess(memberId)) {
      throw new CourseNotFoundException();
    }

    List<String> themeNames =
        course.getThemes().stream().map(ct -> ct.getTheme().getName()).toList();

    List<Integer> regionIds = course.getRegions().stream().map(RegionCourse::getRegionId).toList();
    CourseRegionResult regionResult = regionQueryService.getCourseRegionSummary(regionIds);

    List<CoursePlace> sortedCoursePlaces =
        course.getCoursePlaces().stream()
            .sorted(Comparator.comparingInt(CoursePlace::getOrder))
            .toList();
    List<Integer> placeIds = sortedCoursePlaces.stream().map(CoursePlace::getPlaceId).toList();
    Map<Integer, CoursePlaceItem> placeMap = placeCourseQueryService.findPlacesByIds(placeIds);

    List<CoursePlaceDetailResult> places =
        sortedCoursePlaces.stream()
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
        course.isWriter(memberId),
        places,
        course.getCreatedAt(),
        course.getUpdatedAt());
  }
}

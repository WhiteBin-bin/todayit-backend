package com.todayit.course.service;

import com.todayit.common.pagination.PageResult;
import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.RegionCourse;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.repository.CourseMemberLikeRepository;
import com.todayit.course.repository.CourseScrapRepository;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCourseConditionsCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceEntities;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CopyCourseResult;
import com.todayit.course.service.model.CourseConditionsResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CourseLikeResult;
import com.todayit.course.service.model.CoursePlaceDetailResult;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CourseRegionResult;
import com.todayit.course.service.model.CreateCourseResult;
import com.todayit.course.service.model.MyCourseCardResult;
import com.todayit.course.service.model.MyCourseSort;
import com.todayit.course.service.model.RemoveCoursePlaceResult;
import com.todayit.course.service.model.UpdateCourseConditionsResult;
import com.todayit.course.service.model.UpdateCoursePlaceOrdersResult;
import com.todayit.course.service.model.UpdateCourseTransportResult;
import com.todayit.course.service.model.UpdateCourseVisibilityResult;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 행정 지역 검증과 코스 생성 및 조회를 조정합니다. */
@Service
public class CourseFacade implements MemberCourseQueryService {

  private static final Logger LOG = LoggerFactory.getLogger(CourseFacade.class);

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
   * @throws CourseException 지원하지 않는 콘셉트가 포함된 경우
   */
  @Transactional
  public CreateCourseResult createCourse(String memberId, CreateCourseCommand command) {
    CreateCourseResult result = courseCommandService.createCourse(memberId, command);
    LOG.info("코스 생성 조정 완료: courseId={}", result.courseId());
    return result;
  }

  /**
   * 코스에 장소를 추가하고 추가된 장소 상세 및 정렬된 전체 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 장소 추가 조건
   * @return 추가된 장소 상세 및 전체 정렬된 장소 목록
   * @throws CourseException 코스가 없거나 작성자가 아닌 경우 / 장소가 없거나 삭제된 경우 / 코스 내 동일 장소가 이미 2개인 경우
   */
  @Transactional
  public AddCoursePlaceResult addPlaceToCourse(String memberId, AddCoursePlaceCommand command) {
    if (!placeCourseQueryService.existsActivePlace(command.placeId())) {
      throw new CourseException(CourseErrorCode.PLACE_NOT_FOUND);
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

    LOG.info(
        "코스 장소 추가 조정 완료: courseId={}, coursePlaceId={}, placeId={}, totalPlaceCount={}",
        command.courseId(),
        addedPlace.coursePlaceId(),
        addedPlace.placeId(),
        places.size());
    return new AddCoursePlaceResult(addedPlace, places);
  }

  /**
   * 코스 장소들의 순서를 일괄 변경하고 정렬된 전체 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 장소 순서 일괄 변경 조건
   * @return 순서가 변경된 전체 장소 목록
   * @throws CourseException 코스가 없거나 삭제되었거나 작성자가 아닌 경우 / 장소 목록이 null이거나 중복을 포함하거나 기존 코스 장소 목록과 일치하지
   *     않는 경우
   */
  @Transactional
  public UpdateCoursePlaceOrdersResult updateCoursePlaceOrders(
      String memberId, UpdateCoursePlaceOrdersCommand command) {
    List<CoursePlace> updatedPlaces = courseCommandService.updatePlaceOrders(memberId, command);
    LOG.info(
        "코스 장소 순서 변경 조정 완료: courseId={}, placeCount={}", command.courseId(), updatedPlaces.size());
    return new UpdateCoursePlaceOrdersResult(toCoursePlaceDetailResults(updatedPlaces));
  }

  /**
   * 코스에서 특정 장소를 삭제하고 순서가 재조정된 남은 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param coursePlaceId 삭제할 코스 장소 식별자
   * @return 삭제된 장소 식별자 및 남은 장소 목록
   * @throws CourseException 코스가 없거나 삭제되었거나 작성자가 아닌 경우 / 코스 내에 삭제할 장소가 없는 경우
   */
  @Transactional
  public RemoveCoursePlaceResult removePlaceFromCourse(
      String memberId, int courseId, int coursePlaceId) {
    List<CoursePlace> remainingPlaces =
        courseCommandService.removePlace(memberId, courseId, coursePlaceId);
    LOG.info(
        "코스 장소 삭제 조정 완료: courseId={}, coursePlaceId={}, remainingPlaceCount={}",
        courseId,
        coursePlaceId,
        remainingPlaces.size());
    return new RemoveCoursePlaceResult(coursePlaceId, toCoursePlaceDetailResults(remainingPlaces));
  }

  /**
   * 코스를 영구 삭제합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 삭제할 코스 식별자
   * @throws CourseException 코스가 없거나 삭제되었거나 작성자가 아닌 경우
   */
  @Transactional
  public void deleteCourse(String memberId, int courseId) {
    courseCommandService.deleteCourse(memberId, courseId);
    LOG.info("코스 영구 삭제 조정 완료: courseId={}", courseId);
  }

  /**
   * 다른 회원의 공개 코스 또는 본인의 코스를 복사하여 새로운 DRAFT 코스를 생성합니다.
   *
   * @param memberId 복사를 요청한 회원 식별자
   * @param courseId 복사할 원본 코스 식별자
   * @return 복제된 코스 결과
   * @throws CourseException 코스가 없거나 삭제되었거나 타인의 비공개 코스인 경우 / 임시저장(DRAFT) 상태의 코스를 복사하려는 경우
   */
  @Transactional
  public CopyCourseResult copyCourse(String memberId, int courseId) {
    Course copiedCourse = courseCommandService.copyCourse(memberId, courseId);
    List<CoursePlaceDetailResult> places =
        toCoursePlaceDetailResults(copiedCourse.getCoursePlaces());
    LOG.info(
        "코스 복사 조정 완료: originalCourseId={}, copiedCourseId={}, placeCount={}",
        courseId,
        copiedCourse.getCourseId(),
        places.size());
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
   * @throws CourseException 코스가 없거나 비공개 코스에 접근할 권한이 없는 경우
   */
  @Transactional(readOnly = true)
  public CourseDetailResult getCourseDetail(int courseId, String memberId) {
    Course course = courseQueryService.getCourse(courseId);
    if (!canAccess(course, memberId)) {
      throw new CourseException(CourseErrorCode.NOT_FOUND);
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

    LOG.info(
        "코스 상세 조회 조정 완료: courseId={}, authenticated={}, placeCount={}, liked={}, scrapped={}",
        courseId,
        memberId != null,
        places.size(),
        liked,
        scrapped);
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

  /**
   * 인증된 회원의 코스 좋아요를 등록합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 회원 식별자
   * @return 코스 좋아요 결과
   * @throws CourseException 코스가 없거나 삭제되었거나 임시저장이거나 타인의 비공개 코스인 경우 / 이미 좋아요를 누른 경우
   */
  @Transactional
  public CourseLikeResult likeCourse(int courseId, String memberId) {
    CourseLikeResult result = courseCommandService.likeCourse(courseId, memberId);
    LOG.info("코스 좋아요 등록 조정 완료: courseId={}, likeCount={}", courseId, result.likeCount());
    return result;
  }

  /**
   * 확정된 코스의 공개 범위를 변경합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param visibility 변경할 공개 범위
   * @return 변경된 공개 범위 결과
   * @throws CourseException 코스가 없거나 삭제되었거나 작성자가 아닌 경우 / 임시저장(DRAFT) 코스의 공개 범위를 변경하려는 경우
   */
  @Transactional
  public UpdateCourseVisibilityResult updateCourseVisibility(
      String memberId, int courseId, CourseVisibility visibility) {
    UpdateCourseVisibilityResult result =
        courseCommandService.updateCourseVisibility(memberId, courseId, visibility);
    LOG.info("코스 공개 범위 변경 조정 완료: courseId={}, visibility={}", courseId, result.visibility());
    return result;
  }

  /**
   * 임시저장(DRAFT) 코스의 이동수단을 변경합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param transport 변경할 이동수단
   * @return 변경된 이동수단 결과
   * @throws CourseException 코스가 없거나 삭제되었거나 작성자가 아닌 경우 / 코스가 임시저장(DRAFT) 상태가 아닌 경우
   */
  @Transactional
  public UpdateCourseTransportResult updateCourseTransport(
      String memberId, int courseId, CourseTransport transport) {
    UpdateCourseTransportResult result =
        courseCommandService.updateCourseTransport(memberId, courseId, transport);
    LOG.info("코스 이동수단 변경 조정 완료: courseId={}, transport={}", courseId, result.transport());
    return result;
  }

  /**
   * 임시저장 코스의 생성 조건을 수정하고 갱신된 조건과 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 코스 생성 조건 수정 명령
   * @return 생성 조건 수정 결과
   * @throws CourseException 코스가 없거나 삭제되었거나 작성자가 아닌 경우 / 코스가 임시저장(DRAFT) 상태가 아닌 경우 / 지역 코드가 유효하지
   *     않거나, 소요 시간 계산에 필요한 시작 시각이 없거나, 종료 시각이 시작 시각 이후가 아닌 경우
   */
  @Transactional
  public UpdateCourseConditionsResult updateCourseConditions(
      String memberId, UpdateCourseConditionsCommand command) {
    Course course = courseCommandService.updateCourseConditions(memberId, command);

    List<Integer> regionIds = course.getRegions().stream().map(RegionCourse::getRegionId).toList();
    CourseRegionResult regionResult = regionQueryService.getCourseRegionSummary(regionIds);

    Integer durationMinutes = null;
    if (course.getStartAt() != null && course.getEndAt() != null) {
      durationMinutes = (int) Duration.between(course.getStartAt(), course.getEndAt()).toMinutes();
    }

    CourseConditionsResult conditionsResult =
        new CourseConditionsResult(
            regionResult,
            course.getStartAt(),
            course.getEndAt(),
            durationMinutes,
            course.getTransport());

    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(course.getCoursePlaces());

    LOG.info(
        "코스 생성 조건 수정 조정 완료: courseId={}, transport={}, durationMinutes={}, placeCount={}",
        course.getCourseId(),
        course.getTransport(),
        durationMinutes,
        places.size());
    return new UpdateCourseConditionsResult(
        course.getCourseId(), conditionsResult, places, course.getUpdatedAt());
  }

  private List<CoursePlaceDetailResult> toCoursePlaceDetailResults(
      List<CoursePlace> originalPlaces) {
    List<CoursePlace> sortedPlaces =
        originalPlaces.stream().sorted(Comparator.comparingInt(CoursePlace::getOrder)).toList();

    List<Integer> placeIds = sortedPlaces.stream().map(CoursePlace::getPlaceId).toList();
    Map<Integer, CoursePlaceItem> placeMap = placeCourseQueryService.findPlacesByIds(placeIds);

    return sortedPlaces.stream()
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

  /**
   * 회원이 작성한 코스 목록을 상태 조건 및 페이징에 맞게 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param status 코스 상태 필터, null이면 DELETED 제외 전체 조회
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @param sort 정렬 기준
   * @return 내 코스 카드 페이지
   */
  @Override
  @Transactional(readOnly = true)
  public PageResult<MyCourseCardResult> findMyCourses(
      String memberId, CourseStatus status, int page, int size, MyCourseSort sort) {
    Sort order =
        sort == MyCourseSort.OLDEST
            ? Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("courseId"))
            : Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("courseId"));

    Page<Course> coursePage =
        courseQueryService.findMyCourses(memberId, status, PageRequest.of(page, size, order));
    List<Course> courses = coursePage.getContent();

    if (courses.isEmpty()) {
      LOG.info(
          "내 코스 목록 조회 완료: status={}, page={}, size={}, sort={}, resultCount=0",
          status,
          page,
          size,
          sort);
      return new PageResult<>(
          List.of(), coursePage.getNumber(), coursePage.getSize(), coursePage.getTotalElements());
    }

    List<Integer> firstPlaceIds =
        courses.stream()
            .map(
                c ->
                    c.getCoursePlaces().stream()
                        .min(Comparator.comparingInt(CoursePlace::getOrder))
                        .map(CoursePlace::getPlaceId)
                        .orElse(null))
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    Map<Integer, CoursePlaceItem> placeMap =
        firstPlaceIds.isEmpty() ? Map.of() : placeCourseQueryService.findPlacesByIds(firstPlaceIds);

    List<Integer> courseIds = courses.stream().map(Course::getCourseId).toList();
    List<Object[]> likeCountList = courseMemberLikeRepository.countByCourseIds(courseIds);
    Map<Integer, Long> likeCountMap = new HashMap<>();
    for (Object[] row : likeCountList) {
      likeCountMap.put((Integer) row[0], (Long) row[1]);
    }

    List<MyCourseCardResult> cardList =
        courses.stream()
            .map(
                course -> {
                  List<String> themes =
                      course.getThemes().stream().map(ct -> ct.getTheme().getName()).toList();

                  List<Integer> regionIds =
                      course.getRegions().stream().map(RegionCourse::getRegionId).toList();
                  CourseRegionResult regionSummary =
                      regionQueryService.getCourseRegionSummary(regionIds);

                  Integer firstPlaceId =
                      course.getCoursePlaces().stream()
                          .min(Comparator.comparingInt(CoursePlace::getOrder))
                          .map(CoursePlace::getPlaceId)
                          .orElse(null);

                  String thumbnailUrl = null;
                  if (firstPlaceId != null && placeMap.containsKey(firstPlaceId)) {
                    thumbnailUrl = placeMap.get(firstPlaceId).imageUrl();
                  }

                  long likeCount = likeCountMap.getOrDefault(course.getCourseId(), 0L);

                  return new MyCourseCardResult(
                      course.getCourseId(),
                      course.getTitle(),
                      course.getStatus(),
                      course.getVisibility(),
                      thumbnailUrl,
                      course.getCoursePlaces().size(),
                      regionSummary != null ? regionSummary.sidoName() : null,
                      regionSummary != null ? regionSummary.sigunguName() : null,
                      themes,
                      likeCount,
                      course.getStartAt(),
                      course.getEndAt(),
                      course.getCreatedAt(),
                      course.getUpdatedAt());
                })
            .toList();

    LOG.info(
        "내 코스 목록 조회 완료: status={}, page={}, size={}, sort={}, resultCount={}",
        status,
        page,
        size,
        sort,
        cardList.size());
    return new PageResult<>(
        cardList, coursePage.getNumber(), coursePage.getSize(), coursePage.getTotalElements());
  }
}

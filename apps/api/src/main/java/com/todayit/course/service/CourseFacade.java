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
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CourseSearchCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCourseConditionsCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceEntities;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CopyCourseResult;
import com.todayit.course.service.model.CourseAuthorProfile;
import com.todayit.course.service.model.CourseCardResult;
import com.todayit.course.service.model.CourseConditionsResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CourseDurationFilter;
import com.todayit.course.service.model.CourseLikeResult;
import com.todayit.course.service.model.CoursePlaceDetailResult;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CourseScrapResult;
import com.todayit.course.service.model.CourseScrapSort;
import com.todayit.course.service.model.CourseSort;
import com.todayit.course.service.model.CreateCourseResult;
import com.todayit.course.service.model.MyCourseCardResult;
import com.todayit.course.service.model.MyCourseSort;
import com.todayit.course.service.model.RemoveCoursePlaceResult;
import com.todayit.course.service.model.UpdateCourseConditionsResult;
import com.todayit.course.service.model.UpdateCoursePlaceOrdersResult;
import com.todayit.course.service.model.UpdateCourseTransportResult;
import com.todayit.course.service.model.UpdateCourseVisibilityResult;
import com.todayit.region.service.RegionQueryService;
import com.todayit.region.service.model.RegionSummaryResult;
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
  private final CourseReactionQueryService courseReactionQueryService;
  private final MemberCourseAuthorQueryService memberCourseAuthorQueryService;

  /**
   * 지역 조회, 코스 저장·조회, 장소 조회, 반응 조회 및 작성자 조회 서비스를 받습니다.
   *
   * @param regionQueryService 지역 검증 서비스
   * @param courseCommandService 코스 저장 서비스
   * @param courseQueryService 코스 조회 서비스
   * @param placeCourseQueryService 장소 조회 서비스
   * @param courseReactionQueryService 코스 반응 조회 서비스
   * @param memberCourseAuthorQueryService 코스 작성자 프로필 조회 서비스
   */
  public CourseFacade(
      RegionQueryService regionQueryService,
      CourseCommandService courseCommandService,
      CourseQueryService courseQueryService,
      PlaceCourseQueryService placeCourseQueryService,
      CourseReactionQueryService courseReactionQueryService,
      MemberCourseAuthorQueryService memberCourseAuthorQueryService) {
    this.regionQueryService = regionQueryService;
    this.courseCommandService = courseCommandService;
    this.courseQueryService = courseQueryService;
    this.placeCourseQueryService = placeCourseQueryService;
    this.courseReactionQueryService = courseReactionQueryService;
    this.memberCourseAuthorQueryService = memberCourseAuthorQueryService;
  }

  /**
   * 공개·확정·미삭제 코스를 필터와 정렬 및 페이징 조건에 맞게 조회합니다.
   *
   * <p>검증된 시간 저장 기능이 연결되기 전에는 시간 구간(ALL 제외) 요청의 모든 코스를 미확인으로 제외합니다.
   *
   * @param command 검증된 코스 목록·필터 조회 조건
   * @return 공개 코스 카드 페이지
   */
  @Transactional(readOnly = true)
  public PageResult<CourseCardResult> findCourses(CourseSearchCommand command) {
    courseQueryService.validateThemes(command.themes());

    boolean regionFiltered = command.regions() != null && !command.regions().isEmpty();
    List<Integer> regionIds =
        regionFiltered ? regionQueryService.findRegionIds(command.regions()) : List.of();

    Sort order =
        command.sort() == CourseSort.POPULAR
            ? Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("courseId"))
            : Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("courseId"));

    PageRequest pageable = PageRequest.of(command.page(), command.size(), order);
    Page<Course> coursePage =
        command.duration() == CourseDurationFilter.ALL
            ? courseQueryService.findPublicCourses(
                regionIds, command.themes(), command.transport(), pageable)
            : courseQueryService.findPublicCoursesInDuration(
                regionIds, command.themes(), command.transport(), command.duration(), pageable);

    List<Course> courses = coursePage.getContent();
    if (courses.isEmpty()) {
      LOG.info(
          "코스 목록 조회 완료: regionFiltered={}, themes={}, transport={}, sort={}, page={}, size={}, resultCount=0",
          regionFiltered,
          command.themes(),
          command.transport(),
          command.sort(),
          command.page(),
          command.size());
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
    List<Object[]> likeCountList = courseReactionQueryService.countLikes(courseIds);
    Map<Integer, Long> likeCountMap = new HashMap<>();
    for (Object[] row : likeCountList) {
      likeCountMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
    }

    List<CourseCardResult> cardList =
        courses.stream()
            .map(
                course -> {
                  List<String> themes =
                      course.getThemes().stream()
                          .map(ct -> ct.getTheme().getName())
                          .sorted()
                          .toList();

                  List<Integer> courseRegionIds =
                      course.getRegions().stream().map(RegionCourse::getRegionId).toList();
                  RegionSummaryResult regionSummary =
                      regionQueryService.getRegionSummary(courseRegionIds);

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

                  Integer durationMinutes = null;
                  if (course.getStartAt() != null
                      && course.getEndAt() != null
                      && course.getEndAt().isAfter(course.getStartAt())) {
                    durationMinutes =
                        (int) Duration.between(course.getStartAt(), course.getEndAt()).toMinutes();
                  }

                  return new CourseCardResult(
                      course.getCourseId(),
                      course.getTitle(),
                      course.getCourseSummary(),
                      thumbnailUrl,
                      regionSummary,
                      themes,
                      durationMinutes,
                      course.getTransport(),
                      course.getStatus(),
                      course.getVisibility(),
                      course.getCoursePlaces().size(),
                      likeCount,
                      course.getCreatedAt(),
                      course.getUpdatedAt());
                })
            .toList();

    LOG.info(
        "코스 목록 조회 완료: regionFiltered={}, themes={}, transport={}, sort={}, page={}, size={}, resultCount={}",
        regionFiltered,
        command.themes(),
        command.transport(),
        command.sort(),
        command.page(),
        command.size(),
        cardList.size());
    return new PageResult<>(
        cardList, coursePage.getNumber(), coursePage.getSize(), coursePage.getTotalElements());
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
            .orElseThrow(() -> new CourseException(CourseErrorCode.NOT_FOUND));
    LOG.info(
        "코스 장소 추가 조정 완료: courseId={}, coursePlaceId={}",
        command.courseId(),
        addedPlace.coursePlaceId());
    return new AddCoursePlaceResult(addedPlace, places);
  }

  /**
   * 코스 장소의 방문 순서를 일괄 변경하고 변경된 전체 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 순서 변경 조건
   * @return 순서 변경 후 정렬된 전체 장소 목록
   * @throws CourseException 코스가 없거나 작성자가 아닌 경우 / 요청된 장소 목록이 코스와 일치하지 않는 경우
   */
  @Transactional
  public UpdateCoursePlaceOrdersResult updateCoursePlaceOrders(
      String memberId, UpdateCoursePlaceOrdersCommand command) {
    List<CoursePlace> updatedPlaces = courseCommandService.updatePlaceOrders(memberId, command);
    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(updatedPlaces);
    LOG.info("코스 장소 순서 변경 조정 완료: courseId={}", command.courseId());
    return new UpdateCoursePlaceOrdersResult(places);
  }

  /**
   * 코스에서 특정 장소를 삭제하고, 삭제된 장소 식별자와 재조정된 남은 장소 목록을 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param coursePlaceId 삭제할 코스 장소 식별자
   * @return 삭제된 코스 장소 식별자 및 남은 장소 목록
   * @throws CourseException 코스가 없거나 작성자가 아닌 경우 / 코스 내 해당 장소가 존재하지 않는 경우
   */
  @Transactional
  public RemoveCoursePlaceResult removePlaceFromCourse(
      String memberId, int courseId, int coursePlaceId) {
    List<CoursePlace> remainingPlaces =
        courseCommandService.removePlace(memberId, courseId, coursePlaceId);
    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(remainingPlaces);
    LOG.info("코스 장소 삭제 조정 완료: courseId={}, coursePlaceId={}", courseId, coursePlaceId);
    return new RemoveCoursePlaceResult(coursePlaceId, places);
  }

  /**
   * 작성자 본인의 코스를 영구 삭제합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 삭제할 코스 식별자
   * @throws CourseException 코스가 없거나 작성자가 아닌 경우
   */
  @Transactional
  public void deleteCourse(String memberId, int courseId) {
    courseCommandService.deleteCourse(memberId, courseId);
    LOG.info("코스 영구 삭제 조정 완료: courseId={}", courseId);
  }

  /**
   * 확정된 코스의 장소 목록과 순서를 복제하여 새로운 DRAFT 코스를 생성합니다.
   *
   * @param memberId 인증된 회원 식별자
   * @param courseId 복사할 원본 코스 식별자
   * @return 복사되어 생성된 새 코스의 상세 결과
   * @throws CourseException 원본 코스가 없거나, 삭제되었거나, 타인의 비공개 코스이거나, DRAFT 상태인 경우
   */
  @Transactional
  public CopyCourseResult copyCourse(String memberId, int courseId) {
    Course copiedCourse = courseCommandService.copyCourse(memberId, courseId);
    List<CoursePlaceDetailResult> places =
        toCoursePlaceDetailResults(copiedCourse.getCoursePlaces());
    LOG.info(
        "코스 복사 조정 완료: originalCourseId={}, copiedCourseId={}",
        courseId,
        copiedCourse.getCourseId());
    return new CopyCourseResult(
        copiedCourse.getCourseId(),
        courseId,
        copiedCourse.getTitle(),
        copiedCourse.getStatus(),
        places,
        copiedCourse.getCreatedAt());
  }

  private List<CoursePlaceDetailResult> toCoursePlaceDetailResults(List<CoursePlace> coursePlaces) {
    List<Integer> placeIds = coursePlaces.stream().map(CoursePlace::getPlaceId).distinct().toList();
    Map<Integer, CoursePlaceItem> placeMap =
        placeIds.isEmpty() ? Map.of() : placeCourseQueryService.findPlacesByIds(placeIds);
    return coursePlaces.stream()
        .sorted(Comparator.comparingInt(CoursePlace::getOrder))
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

  /**
   * 코스 상세 정보를 조회하고 지역, 장소, 콘셉트 정보를 조합하여 반환합니다.
   *
   * <p>비공개(PRIVATE) 코스와 임시저장(DRAFT) 코스는 작성자 본인만 조회할 수 있습니다. 권한이 없는 코스는 존재 여부를 노출하지 않도록 404
   * NOT_FOUND로 응답합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 요청 회원 식별자, 비회원이면 null
   * @return 조합된 코스 상세 정보
   * @throws CourseException 코스가 없거나 접근 권한이 없는 경우
   */
  @Transactional(readOnly = true)
  public CourseDetailResult getCourseDetail(int courseId, String memberId) {
    Course course = courseQueryService.getCourse(courseId);
    boolean isWriter = memberId != null && memberId.equals(course.getMemberId());
    if (!canAccessCourse(course, isWriter)) {
      LOG.warn("코스 상세 조회 권한 없음: courseId={}, isWriter={}", courseId, isWriter);
      throw new CourseException(CourseErrorCode.NOT_FOUND);
    }

    CourseAuthorProfile authorProfile =
        memberCourseAuthorQueryService.getAuthorProfile(course.getMemberId());

    List<Integer> regionIds = course.getRegions().stream().map(RegionCourse::getRegionId).toList();
    RegionSummaryResult regionSummary = regionQueryService.getRegionSummary(regionIds);

    List<String> themes = course.getThemes().stream().map(ct -> ct.getTheme().getName()).toList();
    List<CoursePlaceDetailResult> places = toCoursePlaceDetailResults(course.getCoursePlaces());

    boolean liked = memberId != null && courseReactionQueryService.isLiked(memberId, courseId);
    boolean scrapped =
        memberId != null && courseReactionQueryService.isScrapped(memberId, courseId);

    LOG.info(
        "코스 상세 조회 조정 완료: courseId={}, isWriter={}, placeCount={}",
        courseId,
        isWriter,
        places.size());
    return new CourseDetailResult(
        course.getCourseId(),
        course.getTitle(),
        course.getCourseSummary(),
        authorProfile.displayName(),
        authorProfile.profileImageUrl(),
        course.getTransport(),
        course.getStartAt(),
        course.getEndAt(),
        course.getStatus(),
        course.getVisibility(),
        course.getViewCount(),
        regionSummary,
        themes,
        isWriter,
        places,
        course.getCreatedAt(),
        course.getUpdatedAt(),
        liked,
        scrapped,
        course.getBeforeVisit());
  }

  private boolean canAccessCourse(Course course, boolean isWriter) {
    if (isWriter) {
      return true;
    }
    return course.getStatus() == CourseStatus.ACTIVE
        && course.getVisibility() == CourseVisibility.PUBLIC;
  }

  /**
   * 인증된 회원의 코스 좋아요 등록을 조정합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 인증된 회원 식별자
   * @return 코스 좋아요 등록 결과
   */
  @Transactional
  public CourseLikeResult likeCourse(int courseId, String memberId) {
    CourseLikeResult result = courseCommandService.likeCourse(courseId, memberId);
    LOG.info(
        "코스 좋아요 등록 조정 완료: courseId={}, liked={}, likeCount={}",
        result.courseId(),
        result.liked(),
        result.likeCount());
    return result;
  }

  /**
   * 인증된 회원의 코스 스크랩 등록을 조정합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 인증된 회원 식별자
   * @return 코스 스크랩 등록 결과
   */
  @Transactional
  public CourseScrapResult scrapCourse(int courseId, String memberId) {
    CourseScrapResult result = courseCommandService.scrapCourse(courseId, memberId);
    LOG.info(
        "코스 스크랩 등록 조정 완료: courseId={}, scrapped={}, scrapCount={}",
        result.courseId(),
        result.scrapped(),
        result.scrapCount());
    return result;
  }

  /**
   * 인증된 회원의 코스 스크랩 취소를 조정합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 인증된 회원 식별자
   * @return 코스 스크랩 취소 결과
   */
  @Transactional
  public CourseScrapResult cancelCourseScrap(int courseId, String memberId) {
    CourseScrapResult result = courseCommandService.cancelCourseScrap(courseId, memberId);
    LOG.info(
        "코스 스크랩 취소 조정 완료: courseId={}, scrapped={}, scrapCount={}",
        result.courseId(),
        result.scrapped(),
        result.scrapCount());
    return result;
  }

  /**
   * 회원이 작성한 코스 목록을 조건에 맞게 페이지 단위로 조회하고 카드 모델로 조립합니다.
   *
   * @param memberId 인증된 회원 식별자
   * @param status 코스 상태 필터, null이면 전체(DELETED 제외) 조회
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지 크기 (1 이상)
   * @param sort 정렬 기준 (LATEST 또는 OLDEST)
   * @return 내 코스 목록 페이지 결과
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

    List<MyCourseCardResult> cardList = toMyCourseCardResults(courses);

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

  /**
   * 회원이 스크랩한 코스 목록을 조건에 맞게 페이지 단위로 조회하고 카드 모델로 조립합니다.
   *
   * @param memberId 인증된 회원 식별자
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지 크기 (1 이상)
   * @param sort 스크랩 정렬 기준 (LATEST 또는 OLDEST)
   * @return 스크랩 코스 목록 페이지 결과
   */
  @Override
  @Transactional(readOnly = true)
  public PageResult<MyCourseCardResult> findScrappedCourses(
      String memberId, int page, int size, CourseScrapSort sort) {
    Page<Course> coursePage = courseQueryService.findScrappedCourses(memberId, page, size, sort);
    List<Course> courses = coursePage.getContent();

    if (courses.isEmpty()) {
      LOG.info("스크랩 코스 목록 조회 완료: page={}, size={}, sort={}, resultCount=0", page, size, sort);
      return new PageResult<>(
          List.of(), coursePage.getNumber(), coursePage.getSize(), coursePage.getTotalElements());
    }

    List<MyCourseCardResult> cardList = toMyCourseCardResults(courses);

    LOG.info(
        "스크랩 코스 목록 조회 완료: page={}, size={}, sort={}, resultCount={}",
        page,
        size,
        sort,
        cardList.size());
    return new PageResult<>(
        cardList, coursePage.getNumber(), coursePage.getSize(), coursePage.getTotalElements());
  }

  private List<MyCourseCardResult> toMyCourseCardResults(List<Course> courses) {
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
    List<Object[]> likeCountList = courseReactionQueryService.countLikes(courseIds);
    Map<Integer, Long> likeCountMap = new HashMap<>();
    for (Object[] row : likeCountList) {
      likeCountMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
    }

    return courses.stream()
        .map(
            course -> {
              List<String> themes =
                  course.getThemes().stream().map(ct -> ct.getTheme().getName()).toList();

              List<Integer> regionIds =
                  course.getRegions().stream().map(RegionCourse::getRegionId).toList();
              RegionSummaryResult regionSummary = regionQueryService.getRegionSummary(regionIds);
              String sidoName = regionSummary != null ? regionSummary.sidoName() : null;
              String sigunguName = regionSummary != null ? regionSummary.sigunguName() : null;

              Integer firstPlaceId =
                  course.getCoursePlaces().stream()
                      .min(Comparator.comparingInt(CoursePlace::getOrder))
                      .map(CoursePlace::getPlaceId)
                      .orElse(null);

              String thumbnailUrl = null;
              if (firstPlaceId != null && placeMap.containsKey(firstPlaceId)) {
                thumbnailUrl = placeMap.get(firstPlaceId).imageUrl();
              }

              int placeCount = course.getCoursePlaces().size();
              long likeCount = likeCountMap.getOrDefault(course.getCourseId(), 0L);

              return new MyCourseCardResult(
                  course.getCourseId(),
                  course.getTitle(),
                  course.getStatus(),
                  course.getVisibility(),
                  thumbnailUrl,
                  placeCount,
                  sidoName,
                  sigunguName,
                  themes,
                  likeCount,
                  course.getStartAt(),
                  course.getEndAt(),
                  course.getCreatedAt(),
                  course.getUpdatedAt());
            })
        .toList();
  }

  /**
   * 확정된 코스의 공개 범위를 수정합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param visibility 변경할 공개 범위
   * @return 변경된 공개 범위 결과
   */
  @Transactional
  public UpdateCourseVisibilityResult updateCourseVisibility(
      String memberId, int courseId, CourseVisibility visibility) {
    UpdateCourseVisibilityResult result =
        courseCommandService.updateCourseVisibility(memberId, courseId, visibility);
    LOG.info(
        "코스 공개 범위 변경 조정 완료: courseId={}, visibility={}", result.courseId(), result.visibility());
    return result;
  }

  /**
   * 임시저장 코스의 생성 조건을 수정하고, 수정된 조건과 장소 목록을 조합하여 반환합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param command 코스 생성 조건 수정 명령
   * @return 수정된 코스 생성 조건 및 장소 목록
   */
  @Transactional
  public UpdateCourseConditionsResult updateCourseConditions(
      String memberId, UpdateCourseConditionsCommand command) {
    Course updatedCourse = courseCommandService.updateCourseConditions(memberId, command);

    List<Integer> regionIds =
        updatedCourse.getRegions().stream().map(RegionCourse::getRegionId).toList();
    RegionSummaryResult regionSummary = regionQueryService.getRegionSummary(regionIds);

    Integer durationMinutes = null;
    if (updatedCourse.getStartAt() != null && updatedCourse.getEndAt() != null) {
      durationMinutes =
          (int) Duration.between(updatedCourse.getStartAt(), updatedCourse.getEndAt()).toMinutes();
    }

    CourseConditionsResult conditionsResult =
        new CourseConditionsResult(
            regionSummary,
            updatedCourse.getStartAt(),
            updatedCourse.getEndAt(),
            durationMinutes,
            updatedCourse.getTransport());

    List<CoursePlaceDetailResult> places =
        toCoursePlaceDetailResults(updatedCourse.getCoursePlaces());
    long scrapCount = courseReactionQueryService.countScraps(updatedCourse.getCourseId());

    LOG.info("코스 조건 변경 조정 완료: courseId={}", updatedCourse.getCourseId());
    return new UpdateCourseConditionsResult(
        updatedCourse.getCourseId(),
        conditionsResult,
        places,
        updatedCourse.getUpdatedAt(),
        scrapCount);
  }

  /**
   * 임시저장 코스의 이동수단 변경을 조정합니다.
   *
   * @param memberId 인증된 작성자 식별자
   * @param courseId 코스 식별자
   * @param transport 변경할 이동수단
   * @return 변경된 이동수단 결과
   */
  @Transactional
  public UpdateCourseTransportResult updateCourseTransport(
      String memberId, int courseId, CourseTransport transport) {
    UpdateCourseTransportResult result =
        courseCommandService.updateCourseTransport(memberId, courseId, transport);
    LOG.info("코스 이동수단 변경 조정 완료: courseId={}, transport={}", result.courseId(), result.transport());
    return result;
  }
}

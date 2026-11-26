package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayit.common.pagination.PageResult;
import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.RegionCourse;
import com.todayit.course.entity.Theme;
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
import com.todayit.course.service.model.CourseCardResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CourseDurationFilter;
import com.todayit.course.service.model.CourseLikeResult;
import com.todayit.course.service.model.CoursePlaceItem;
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
import com.todayit.region.service.model.RegionFilter;
import com.todayit.region.service.model.RegionSummaryResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CourseFacadeTest {

  @Mock private RegionQueryService regions;
  @Mock private CourseCommandService courses;
  @Mock private CourseQueryService courseQueryService;
  @Mock private PlaceCourseQueryService placeCourseQueryService;
  @Mock private CourseReactionQueryService courseReactionQueryService;

  @InjectMocks private CourseFacade facade;

  @Test
  @DisplayName("공개 코스 목록을 필터와 정렬에 맞게 조회하고 카드 모델 페이지로 반환한다")
  void findsCoursesWithFiltersSuccessfully() {
    // given
    Theme theme = mock(Theme.class);
    when(theme.getName()).thenReturn("로맨틱");

    Course course =
        Course.createDraft(
            "member-1",
            "행궁동 데이트 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 11, 24, 10, 0),
            LocalDateTime.of(2026, 11, 24, 18, 0),
            List.of(theme),
            LocalDateTime.of(2026, 11, 24, 9, 0));
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "courseSummary", "행궁동 감성 산책 코스");
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);
    course.getRegions().add(RegionCourse.create(course, 7));
    addPlace(course, 10, 1, 60);

    List<RegionFilter> regionFilters = List.of(new RegionFilter("41", List.of("111")));
    CourseSearchCommand command =
        new CourseSearchCommand(
            regionFilters,
            List.of("로맨틱"),
            null,
            CourseTransport.WALKING,
            CourseSort.POPULAR,
            0,
            20);

    when(regions.findRegionIds(regionFilters)).thenReturn(List.of(7));
    when(courseQueryService.findPublicCourses(
            eq(List.of(7)),
            eq(List.of("로맨틱")),
            eq(CourseTransport.WALKING),
            any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(course), PageRequest.of(0, 20), 1));
    when(placeCourseQueryService.findPlacesByIds(List.of(10)))
        .thenReturn(
            Map.of(
                10,
                new CoursePlaceItem(
                    10,
                    "장소 1",
                    "CAFE",
                    "주소 1",
                    BigDecimal.valueOf(37.1),
                    BigDecimal.valueOf(127.1),
                    "https://example.com/place.jpg")));
    when(courseReactionQueryService.countLikes(List.of(1)))
        .thenReturn(List.<Object[]>of(new Object[] {1, 15L}));
    when(regions.getRegionSummary(List.of(7)))
        .thenReturn(new RegionSummaryResult("41", "경기도", "111", "수원시 장안구"));

    // when
    PageResult<CourseCardResult> result = facade.findCourses(command);

    // then
    verify(courseQueryService).validateThemes(List.of("로맨틱"));
    assertThat(result.totalElements()).isEqualTo(1L);
    assertThat(result.content()).hasSize(1);
    CourseCardResult card = result.content().getFirst();
    assertThat(card.courseId()).isEqualTo(1);
    assertThat(card.title()).isEqualTo("행궁동 데이트 코스");
    assertThat(card.courseSummary()).isEqualTo("행궁동 감성 산책 코스");
    assertThat(card.thumbnailUrl()).isEqualTo("https://example.com/place.jpg");
    assertThat(card.region().sidoName()).isEqualTo("경기도");
    assertThat(card.region().sigunguName()).isEqualTo("수원시 장안구");
    assertThat(card.themes()).containsExactly("로맨틱");
    assertThat(card.durationMinutes()).isEqualTo(480);
    assertThat(card.transport()).isEqualTo(CourseTransport.WALKING);
    assertThat(card.status()).isEqualTo(CourseStatus.ACTIVE);
    assertThat(card.visibility()).isEqualTo(CourseVisibility.PUBLIC);
    assertThat(card.placeCount()).isEqualTo(1);
    assertThat(card.likeCount()).isEqualTo(15L);
  }

  @Test
  @DisplayName("소요 시간 구간 필터를 조회 서비스에 전달한다")
  void returnsEmptyPageWhenDurationFilterIsPresent() {
    // given
    CourseSearchCommand command =
        new CourseSearchCommand(
            List.of(),
            List.of("로맨틱"),
            CourseDurationFilter.TWO_TO_FOUR_HOURS,
            null,
            CourseSort.LATEST,
            0,
            20);

    when(courseQueryService.findPublicCoursesInDuration(any(), any(), any(), any(), any()))
        .thenReturn(org.springframework.data.domain.Page.empty());
    // when
    PageResult<CourseCardResult> result = facade.findCourses(command);

    // then
    verify(courseQueryService).validateThemes(List.of("로맨틱"));
    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
    verifyNoInteractions(placeCourseQueryService);
  }

  @Test
  @DisplayName("코스 생성 명령을 CourseCommandService에 정상적으로 위임한다")
  void createsCourseSuccessfully() {
    // given
    CreateCourseCommand command =
        new CreateCourseCommand(
            "코스",
            List.of("CAFE"),
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            CourseTransport.WALKING);
    CreateCourseResult expected = new CreateCourseResult(1);
    when(courses.createCourse("member-1", command)).thenReturn(expected);

    // when
    CreateCourseResult result = facade.createCourse("member-1", command);

    // then
    assertThat(result).isEqualTo(expected);
    verify(courses).createCourse("member-1", command);
  }

  @Test
  @DisplayName("장소가 유효하면 CourseCommandService에 장소 추가를 위임한다")
  void addsPlaceToCourseSuccessfully() {
    // given
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, 1, 60);
    CoursePlace addedPlace = mock(CoursePlace.class);
    when(addedPlace.getCoursePlaceId()).thenReturn(100);
    when(addedPlace.getPlaceId()).thenReturn(10);
    when(addedPlace.getOrder()).thenReturn(1);
    when(addedPlace.getStayMinute()).thenReturn(60);

    AddCoursePlaceEntities entities = new AddCoursePlaceEntities(addedPlace, List.of(addedPlace));
    when(placeCourseQueryService.existsActivePlace(10)).thenReturn(true);
    when(courses.addPlace("member-1", command)).thenReturn(entities);
    when(placeCourseQueryService.findPlacesByIds(List.of(10)))
        .thenReturn(
            Map.of(
                10,
                new CoursePlaceItem(
                    10,
                    "블루보틀 삼청",
                    "CAFE_DESSERT",
                    "서울특별시 종로구 북촌로 5길",
                    BigDecimal.valueOf(37.5),
                    BigDecimal.valueOf(127.0),
                    "image.jpg")));

    // when
    AddCoursePlaceResult result = facade.addPlaceToCourse("member-1", command);

    // then
    assertThat(result.addedPlace().coursePlaceId()).isEqualTo(100);
    assertThat(result.addedPlace().name()).isEqualTo("블루보틀 삼청");
    assertThat(result.places()).hasSize(1);
    verify(courses).addPlace("member-1", command);
  }

  @Test
  @DisplayName("장소가 존재하지 않거나 비활성 상태이면 PlaceNotFoundException을 던진다")
  void throwsExceptionWhenPlaceNotFound() {
    // given
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 999, 1, 60);
    when(placeCourseQueryService.existsActivePlace(999)).thenReturn(false);

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> facade.addPlaceToCourse("member-1", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.PLACE_NOT_FOUND);
    verifyNoInteractions(courses);
  }

  @Test
  @DisplayName("코스 상세 정보를 정상적으로 조합하여 반환한다")
  void getsCourseDetailSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    addPlace(course, 10, null, 60);

    RegionSummaryResult regionResult = new RegionSummaryResult("11", "서울특별시", "110", "종로구");
    CoursePlaceItem placeItem =
        new CoursePlaceItem(
            10,
            "블루보틀 삼청",
            "CAFE_DESSERT",
            "서울특별시 종로구 북촌로 5길",
            new BigDecimal("37.58123400"),
            new BigDecimal("126.98123400"),
            "https://example.com/place.jpg");

    when(courseQueryService.getCourse(1)).thenReturn(course);
    when(regions.getRegionSummary(List.of())).thenReturn(regionResult);
    when(placeCourseQueryService.findPlacesByIds(List.of(10))).thenReturn(Map.of(10, placeItem));
    when(courseReactionQueryService.isLiked("member-1", 1)).thenReturn(true);
    when(courseReactionQueryService.isScrapped("member-1", 1)).thenReturn(false);

    // when
    CourseDetailResult result = facade.getCourseDetail(1, "member-1");

    // then
    assertThat(result.title()).isEqualTo("코스");
    assertThat(result.sidoName()).isEqualTo("서울특별시");
    assertThat(result.sigunguName()).isEqualTo("종로구");
    assertThat(result.isWriter()).isTrue();
    assertThat(result.liked()).isTrue();
    assertThat(result.scrapped()).isFalse();
    assertThat(result.beforeVisit()).isEmpty();
    assertThat(result.places()).hasSize(1);
    assertThat(result.places().get(0).name()).isEqualTo("블루보틀 삼청");
    assertThat(result.places().get(0).stayMinute()).isEqualTo(60);
  }

  @Test
  @DisplayName("지역 정보가 없는 코스도 NullPointerException 없이 조회된다")
  void getsCourseDetailWithoutRegionsSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "지역 없는 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());

    when(courseQueryService.getCourse(1)).thenReturn(course);
    when(regions.getRegionSummary(List.of())).thenReturn(null);

    // when
    CourseDetailResult result = facade.getCourseDetail(1, "member-1");

    // then
    assertThat(result.title()).isEqualTo("지역 없는 코스");
    assertThat(result.sidoCode()).isNull();
    assertThat(result.sidoName()).isNull();
    assertThat(result.sigunguCode()).isNull();
    assertThat(result.sigunguName()).isNull();
    assertThat(result.places()).isEmpty();
    assertThat(result.liked()).isFalse();
    assertThat(result.scrapped()).isFalse();
    assertThat(result.beforeVisit()).isEmpty();
  }

  @Test
  @DisplayName("접근 권한이 없는 코스 상세 조회 시 CourseException을 던진다")
  void throwsExceptionWhenCannotAccessCourse() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "비공개 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    when(courseQueryService.getCourse(1)).thenReturn(course);

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> facade.getCourseDetail(1, "member-2"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
    verifyNoInteractions(placeCourseQueryService);
  }

  @Test
  @DisplayName("코스 장소 순서 변경을 정상적으로 위임한다")
  void updatesCoursePlaceOrdersSuccessfully() {
    // given
    UpdateCoursePlaceOrdersCommand command =
        new UpdateCoursePlaceOrdersCommand(1, List.of(3, 1, 2));
    CoursePlace p = mock(CoursePlace.class);
    when(p.getCoursePlaceId()).thenReturn(101);
    when(p.getPlaceId()).thenReturn(10);
    when(p.getOrder()).thenReturn(1);
    when(p.getStayMinute()).thenReturn(60);

    when(courses.updatePlaceOrders("member-1", command)).thenReturn(List.of(p));
    when(placeCourseQueryService.findPlacesByIds(List.of(10)))
        .thenReturn(
            Map.of(
                10,
                new CoursePlaceItem(
                    10,
                    "장소",
                    "카페",
                    "주소",
                    BigDecimal.valueOf(37.5),
                    BigDecimal.valueOf(127.0),
                    "image.jpg")));

    // when
    UpdateCoursePlaceOrdersResult result = facade.updateCoursePlaceOrders("member-1", command);

    // then
    assertThat(result.places()).hasSize(1);
    verify(courses).updatePlaceOrders("member-1", command);
  }

  @Test
  @DisplayName("코스 장소 삭제를 정상적으로 위임하고 남은 장소 목록을 반환한다")
  void removesPlaceFromCourseSuccessfully() {
    // given
    CoursePlace remainingPlace = mock(CoursePlace.class);
    when(remainingPlace.getCoursePlaceId()).thenReturn(20);
    when(remainingPlace.getPlaceId()).thenReturn(200);
    when(remainingPlace.getOrder()).thenReturn(1);
    when(remainingPlace.getStayMinute()).thenReturn(30);

    when(courses.removePlace("member-1", 1, 10)).thenReturn(List.of(remainingPlace));
    when(placeCourseQueryService.findPlacesByIds(List.of(200)))
        .thenReturn(
            Map.of(
                200,
                new CoursePlaceItem(
                    200,
                    "장소",
                    "카페",
                    "주소",
                    BigDecimal.valueOf(37.5),
                    BigDecimal.valueOf(127.0),
                    "image.jpg")));

    // when
    RemoveCoursePlaceResult result = facade.removePlaceFromCourse("member-1", 1, 10);

    // then
    assertThat(result.deletedCoursePlaceId()).isEqualTo(10);
    assertThat(result.places()).hasSize(1);
    assertThat(result.places().get(0).coursePlaceId()).isEqualTo(20);
    verify(courses).removePlace("member-1", 1, 10);
  }

  @Test
  @DisplayName("코스 영구 삭제를 정상적으로 위임한다")
  void deletesCourseSuccessfully() {
    // given
    String memberId = "member-1";
    int courseId = 1;

    // when
    facade.deleteCourse(memberId, courseId);

    // then
    verify(courses).deleteCourse(memberId, courseId);
  }

  @Test
  @DisplayName("코스 복사를 정상적으로 위임한다")
  void copiesCourseSuccessfully() {
    // given
    Course copiedCourse = mock(Course.class);
    when(copiedCourse.getCourseId()).thenReturn(2);
    when(copiedCourse.getTitle()).thenReturn(null);
    when(copiedCourse.getStatus()).thenReturn(CourseStatus.DRAFT);
    when(copiedCourse.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 11, 19, 12, 0));
    when(copiedCourse.getCoursePlaces()).thenReturn(List.of());

    when(courses.copyCourse("member-1", 1)).thenReturn(copiedCourse);

    // when
    CopyCourseResult result = facade.copyCourse("member-1", 1);

    // then
    assertThat(result.courseId()).isEqualTo(2);
    assertThat(result.originalCourseId()).isEqualTo(1);
    assertThat(result.title()).isNull();
    assertThat(result.status()).isEqualTo(CourseStatus.DRAFT);
    assertThat(result.places()).isEmpty();
    verify(courses).copyCourse("member-1", 1);
  }

  private CoursePlace addPlace(
      Course course, int placeId, Integer targetOrder, Integer stayMinute) {
    int maxOrder =
        course.getCoursePlaces().stream().mapToInt(CoursePlace::getOrder).max().orElse(0);
    int assignedOrder =
        (targetOrder == null || targetOrder > maxOrder) ? maxOrder + 1 : Math.max(1, targetOrder);
    CoursePlace cp = CoursePlace.create(course, placeId, assignedOrder, stayMinute);
    course.getCoursePlaces().add(cp);
    return cp;
  }

  @Test
  @DisplayName("코스 좋아요 등록을 CourseCommandService에 정상적으로 위임한다")
  void likesCourseSuccessfully() {
    // given
    CourseLikeResult expected = new CourseLikeResult(1, true, 3L);
    when(courses.likeCourse(1, "member-1")).thenReturn(expected);

    // when
    CourseLikeResult result = facade.likeCourse(1, "member-1");

    // then
    assertThat(result).isEqualTo(expected);
    verify(courses).likeCourse(1, "member-1");
  }

  @Test
  @DisplayName("회원의 코스 목록을 정상적으로 조회하고 카드 모델 페이지로 조합한다")
  void findsMyCoursesSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "내 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 11, 24, 10, 0),
            LocalDateTime.of(2026, 11, 24, 18, 0),
            List.of(),
            LocalDateTime.of(2026, 11, 24, 9, 0));
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);
    addPlace(course, 10, 1, 30);

    when(courseQueryService.findMyCourses(
            eq("member-1"), eq(CourseStatus.ACTIVE), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(course), PageRequest.of(0, 20), 1));
    when(placeCourseQueryService.findPlacesByIds(List.of(10)))
        .thenReturn(
            Map.of(
                10,
                new CoursePlaceItem(
                    10,
                    "장소 1",
                    "CAFE",
                    "주소 1",
                    BigDecimal.valueOf(37.1),
                    BigDecimal.valueOf(127.1),
                    "http://image.jpg")));
    when(courseReactionQueryService.countLikes(List.of(1)))
        .thenReturn(List.<Object[]>of(new Object[] {1, 7L}));
    when(regions.getRegionSummary(List.of()))
        .thenReturn(new RegionSummaryResult("11", "서울특별시", "110", "종로구"));

    // when
    PageResult<MyCourseCardResult> result =
        facade.findMyCourses("member-1", CourseStatus.ACTIVE, 0, 20, MyCourseSort.LATEST);

    // then
    assertThat(result.totalElements()).isEqualTo(1L);
    assertThat(result.content()).hasSize(1);
    MyCourseCardResult card = result.content().get(0);
    assertThat(card.courseId()).isEqualTo(1);
    assertThat(card.title()).isEqualTo("내 코스");
    assertThat(card.status()).isEqualTo(CourseStatus.ACTIVE);
    assertThat(card.thumbnailUrl()).isEqualTo("http://image.jpg");
    assertThat(card.placeCount()).isEqualTo(1);
    assertThat(card.likeCount()).isEqualTo(7L);
    assertThat(card.sidoName()).isEqualTo("서울특별시");
    assertThat(card.sigunguName()).isEqualTo("종로구");
  }

  @Test
  @DisplayName("코스 공개 범위 수정을 CourseCommandService에 정상적으로 위임한다")
  void updatesCourseVisibilitySuccessfully() {
    // given
    LocalDateTime now = LocalDateTime.now();
    UpdateCourseVisibilityResult expected =
        new UpdateCourseVisibilityResult(1, CourseVisibility.PUBLIC, now);
    when(courses.updateCourseVisibility("member-1", 1, CourseVisibility.PUBLIC))
        .thenReturn(expected);

    // when
    UpdateCourseVisibilityResult result =
        facade.updateCourseVisibility("member-1", 1, CourseVisibility.PUBLIC);

    // then
    assertThat(result).isEqualTo(expected);
    verify(courses).updateCourseVisibility("member-1", 1, CourseVisibility.PUBLIC);
  }

  @Test
  @DisplayName("코스 생성 조건 수정을 수행하고 요약 지역 정보와 장소 목록을 조립하여 반환한다")
  void updatesCourseConditionsSuccessfully() {
    // given
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    LocalDateTime endAt = LocalDateTime.of(2026, 12, 1, 14, 0);
    LocalDateTime updatedAt = LocalDateTime.of(2026, 11, 24, 18, 0);

    Course course =
        Course.createDraft(
            "member-1",
            "임시 코스",
            CourseTransport.CAR,
            startAt,
            endAt,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "updatedAt", updatedAt);

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, "11", startAt, endAt, 240, CourseTransport.CAR);

    when(courses.updateCourseConditions("member-1", command)).thenReturn(course);
    when(regions.getRegionSummary(any()))
        .thenReturn(new RegionSummaryResult("11", "서울특별시", "110", "종로구"));

    // when
    UpdateCourseConditionsResult result = facade.updateCourseConditions("member-1", command);

    // then
    assertThat(result.courseId()).isEqualTo(1);
    assertThat(result.conditions().region().sidoCode()).isEqualTo("11");
    assertThat(result.conditions().region().sidoName()).isEqualTo("서울특별시");
    assertThat(result.conditions().region().sigunguCode()).isEqualTo("110");
    assertThat(result.conditions().region().sigunguName()).isEqualTo("종로구");
    assertThat(result.conditions().startAt()).isEqualTo(startAt);
    assertThat(result.conditions().endAt()).isEqualTo(endAt);
    assertThat(result.conditions().durationMinutes()).isEqualTo(240);
    assertThat(result.conditions().transport()).isEqualTo(CourseTransport.CAR);
    assertThat(result.updatedAt()).isEqualTo(updatedAt);
  }

  @Test
  @DisplayName("코스 이동수단 변경을 CourseCommandService에 정상적으로 위임한다")
  void updatesCourseTransportSuccessfully() {
    // given
    UpdateCourseTransportResult expected =
        new UpdateCourseTransportResult(1, CourseTransport.WALKING);
    when(courses.updateCourseTransport("member-1", 1, CourseTransport.WALKING))
        .thenReturn(expected);

    // when
    UpdateCourseTransportResult result =
        facade.updateCourseTransport("member-1", 1, CourseTransport.WALKING);

    // then
    assertThat(result).isEqualTo(expected);
    verify(courses).updateCourseTransport("member-1", 1, CourseTransport.WALKING);
  }
}

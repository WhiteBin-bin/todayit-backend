package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.PlaceNotFoundException;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CourseRegionResult;
import com.todayit.course.service.model.CreateCourseResult;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("코스 생성, 상세 조회 및 장소 편집 Facade")
class CourseFacadeTest {
  @Mock private RegionQueryService regions;
  @Mock private CourseCommandService courses;
  @Mock private CourseQueryService courseQueryService;
  @Mock private PlaceCourseQueryService placeCourseQueryService;
  @InjectMocks private CourseFacade facade;

  @Test
  @DisplayName("코스를 정상적으로 생성한다")
  void createsCourseSuccessfully() {
    // given
    CreateCourseCommand command =
        new CreateCourseCommand(
            "코스",
            List.of("감성적"),
            LocalDateTime.of(2026, 12, 1, 10, 0),
            LocalDateTime.of(2026, 12, 1, 14, 0),
            CourseTransport.WALKING);
    CreateCourseResult expected = new CreateCourseResult(1);
    when(courses.createCourse("member-1", command)).thenReturn(expected);

    // when
    CreateCourseResult result = facade.createCourse("member-1", command);

    // then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("활성화된 장소를 코스에 정상적으로 추가한다")
  void addsPlaceToCourseSuccessfully() {
    // given
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, 1, 60);
    AddCoursePlaceResult expected = new AddCoursePlaceResult(100, 10, 1, 60);
    when(placeCourseQueryService.existsActivePlace(10)).thenReturn(true);
    when(courses.addPlace("member-1", command)).thenReturn(expected);

    // when
    AddCoursePlaceResult result = facade.addPlaceToCourse("member-1", command);

    // then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("존재하지 않거나 비활성화된 장소는 코스에 추가할 수 없다")
  void rejectsWhenPlaceDoesNotExist() {
    // given
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 999, 1, 60);
    when(placeCourseQueryService.existsActivePlace(999)).thenReturn(false);

    // when & then
    assertThrows(PlaceNotFoundException.class, () -> facade.addPlaceToCourse("member-1", command));
    verifyNoInteractions(courses);
  }

  @Test
  @DisplayName("코스 상세 정보를 지역 요약 및 장소 정보와 함께 조회한다")
  void getsCourseDetailSuccessfully() {
    // given
    LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
    Course course =
        Course.createDraft(
            "member-1",
            "종로 데이트",
            CourseTransport.WALKING,
            start,
            start.plusHours(4),
            List.of(),
            start.minusDays(1));
    course.addRegion(7);
    course.addCoursePlace(new CoursePlace(course, 10, 1, 60));

    CoursePlaceItem placeItem =
        new CoursePlaceItem(
            10,
            "블루보틀 삼청",
            "CAFE_DESSERT",
            "서울특별시 종로구",
            new BigDecimal("37.58123400"),
            new BigDecimal("126.98123400"),
            "https://example.com/place.jpg");

    when(courseQueryService.getCourse(1)).thenReturn(course);
    when(regions.getCourseRegionSummary(List.of(7)))
        .thenReturn(new CourseRegionResult("11", "서울특별시", "110", "종로구"));
    when(placeCourseQueryService.findPlacesByIds(List.of(10))).thenReturn(Map.of(10, placeItem));

    // when
    CourseDetailResult result = facade.getCourseDetail(1, "member-1");

    // then
    assertThat(result.title()).isEqualTo("종로 데이트");
    assertThat(result.transport()).isEqualTo(CourseTransport.WALKING);
    assertThat(result.sidoCode()).isEqualTo("11");
    assertThat(result.sidoName()).isEqualTo("서울특별시");
    assertThat(result.sigunguCode()).isEqualTo("110");
    assertThat(result.sigunguName()).isEqualTo("종로구");
    assertThat(result.isWriter()).isTrue();
    assertThat(result.places()).hasSize(1);
    assertThat(result.places().get(0).name()).isEqualTo("블루보틀 삼청");
    assertThat(result.places().get(0).imageUrl()).isEqualTo("https://example.com/place.jpg");
    assertThat(result.places().get(0).stayMinute()).isEqualTo(60);
  }

  @Test
  @DisplayName("접근 권한이 없는 비공개 코스는 조회할 수 없다")
  void rejectsInaccessibleCourse() {
    // given
    LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
    Course course =
        Course.createDraft(
            "member-1", "비공개 코스", CourseTransport.WALKING, start, null, List.of(), start);
    when(courseQueryService.getCourse(1)).thenReturn(course);

    // when & then
    assertThrows(CourseNotFoundException.class, () -> facade.getCourseDetail(1, "member-2"));
    verifyNoInteractions(placeCourseQueryService);
  }

  @Test
  @DisplayName("코스 장소 순서 변경을 정상적으로 위임한다")
  void updatesCoursePlaceOrdersSuccessfully() {
    // given
    UpdateCoursePlaceOrdersCommand command =
        new UpdateCoursePlaceOrdersCommand(1, List.of(3, 1, 2));

    // when
    facade.updateCoursePlaceOrders("member-1", command);

    // then
    verify(courses).reorderPlaces("member-1", command);
  }
}

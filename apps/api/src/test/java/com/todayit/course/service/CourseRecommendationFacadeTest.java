package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.Theme;
import com.todayit.course.service.command.CourseRecommendationCommand;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CoursePlaceRegion;
import com.todayit.course.service.model.CourseRecommendationProfile;
import com.todayit.course.service.model.RecommendedCourseResult;
import com.todayit.recommendation.service.RecommendationEngineService;
import com.todayit.region.service.RegionQueryService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CourseRecommendationFacadeTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-26T03:00:00Z"), ZoneId.of("Asia/Seoul"));
  @Mock private CourseRecommendationQueryService queries;
  @Mock private MemberCourseRecommendationQueryService members;
  @Mock private PlaceCourseRegionQueryService regions;
  @Mock private PlaceCourseQueryService places;
  @Mock private RegionQueryService regionSummary;
  private CourseRecommendationFacade facade;

  @BeforeEach
  void setUp() {
    facade =
        new CourseRecommendationFacade(
            queries,
            members,
            regions,
            places,
            regionSummary,
            new RecommendationEngineService(),
            CLOCK);
  }

  @Test
  @DisplayName("추천 점수가 같으면 공개일(updatedAt) 최신순, courseId 오름차순으로 정렬한다")
  void ordersEqualScoresByUpdatedTimeThenNumericId() {
    // given
    when(members.getRecommendationProfile("member"))
        .thenReturn(new CourseRecommendationProfile(false, null, null));
    Course older = course(1);
    Course newer10 = course(10);
    Course newer2 = course(2);
    ReflectionTestUtils.setField(older, "updatedAt", LocalDateTime.now(CLOCK).minusDays(1));
    ReflectionTestUtils.setField(newer10, "updatedAt", LocalDateTime.now(CLOCK));
    ReflectionTestUtils.setField(newer2, "updatedAt", LocalDateTime.now(CLOCK));
    when(queries.findCandidates()).thenReturn(List.of(newer10, older, course(3), newer2));

    // when
    List<RecommendedCourseResult> results =
        facade.recommend("member", command(null, List.of(), null, null));

    // then
    assertThat(results).extracting(RecommendedCourseResult::courseId).containsExactly(2, 10, 1, 3);
  }

  @Test
  @DisplayName("개인화 미동의 회원은 활동 이력 없이 최근 14일 인기도로 추천 순위를 계산하고 동점 시 courseId 오름차순으로 정렬한다")
  void ranksByRecentPopularityWithoutPersonalizationAndUsesNumericIdForTies() {
    // given
    when(members.getRecommendationProfile("member"))
        .thenReturn(new CourseRecommendationProfile(false, "11", "110"));
    when(queries.findCandidates())
        .thenReturn(List.of(course(10, "감성적"), course(2, "감성적"), course(3, "조용함")));
    when(queries.countRecentLikes(any(), any()))
        .thenReturn(Collections.singletonList(new Object[] {3, 1L}));
    when(queries.countRecentScraps(any(), any()))
        .thenReturn(Collections.singletonList(new Object[] {10, 1L}));

    // when
    List<RecommendedCourseResult> results =
        facade.recommend("member", command(null, List.of(), null, null));

    // then
    assertThat(results).extracting(RecommendedCourseResult::courseId).containsExactly(10, 3, 2);
    verify(queries, never()).findRecentHistory(any(), any(), any());
    LocalDateTime now = LocalDateTime.now(CLOCK);
    verify(queries).countRecentLikes(now.minusDays(14), now);
    verify(queries).countRecentScraps(now.minusDays(14), now);
  }

  @Test
  @DisplayName("최근 30일 활동 이력의 콘셉트 Jaccard 유사도를 계산하며 콘셉트가 없는 기록은 평균에서 제외한다")
  void comparesHistoryWithJaccardSimilarityAndExcludesEmptyConceptHistory() {
    // given
    when(members.getRecommendationProfile("member"))
        .thenReturn(new CourseRecommendationProfile(true, null, null));
    when(queries.findCandidates()).thenReturn(List.of(course(2, "감성적", "조용함"), course(10, "감성적")));
    when(queries.findRecentHistory(any(), any(), any()))
        .thenReturn(List.of(course(30, "감성적"), course(31)));

    // when
    List<RecommendedCourseResult> results =
        facade.recommend("member", command(null, List.of(), null, null));

    // then
    assertThat(results).extracting(RecommendedCourseResult::courseId).containsExactly(10, 2);
    LocalDateTime now = LocalDateTime.now(CLOCK);
    verify(queries).findRecentHistory("member", now.minusDays(30), now);
  }

  @Test
  @DisplayName("장소 지역, 테마(콘셉트), 이동수단 필터를 함께 적용하고 첫 번째 장소 이미지를 대표 이미지로 설정한다")
  void filtersByPlaceRegionConceptAndTransportTogether() {
    // given
    when(members.getRecommendationProfile("member"))
        .thenReturn(new CourseRecommendationProfile(true, null, null));
    Course matching = course(1, "감성적");
    matching.getCoursePlaces().add(CoursePlace.create(matching, 12, 1, 30));
    Course other = course(2, "감성적");
    other.getCoursePlaces().add(CoursePlace.create(other, 13, 1, 30));
    when(queries.findCandidates()).thenReturn(List.of(other, matching));
    when(regions.findPlaceRegions(any()))
        .thenReturn(
            List.of(
                new CoursePlaceRegion(12, "11", "110"), new CoursePlaceRegion(13, "41", "110")));
    when(places.findPlacesByIds(any()))
        .thenReturn(Map.of(12, new CoursePlaceItem(12, "카페", "CAFE", null, null, null, "image")));

    // when
    List<RecommendedCourseResult> results =
        facade.recommend("member", command("11110", List.of("감성적"), null, CourseTransport.CAR));

    // then
    assertThat(results).extracting(RecommendedCourseResult::courseId).containsExactly(1);
    assertThat(results.getFirst().thumbnailUrl()).isEqualTo("image");
  }

  @Test
  @DisplayName("durationMinutes 필터가 있으면 방문 예정 시각 차이를 대체 사용하지 않고 총 예상시간이 미확인인 코스를 제외한다")
  void excludesUnverifiedTimesInsteadOfUsingVisitSchedule() {
    // given
    when(members.getRecommendationProfile("member"))
        .thenReturn(new CourseRecommendationProfile(true, null, null));

    // when
    List<RecommendedCourseResult> results =
        facade.recommend("member", command(null, List.of(), 120, null));

    // then
    assertThat(results).isEmpty();
    verify(queries, never()).findCandidates();
  }

  @Test
  @DisplayName("추천 후보 코스가 없으면 빈 목록을 반환한다")
  void returnsEmptyArrayWhenNoCandidatesExist() {
    // given
    when(members.getRecommendationProfile("member"))
        .thenReturn(new CourseRecommendationProfile(false, null, null));

    // when
    List<RecommendedCourseResult> results =
        facade.recommend("member", command(null, List.of(), null, null));

    // then
    assertThat(results).isEmpty();
    verifyNoInteractions(places, regions);
  }

  private CourseRecommendationCommand command(
      String region, List<String> themes, Integer duration, CourseTransport transport) {
    return new CourseRecommendationCommand(region, themes, duration, transport);
  }

  private Course course(int id, String... names) {
    List<Theme> themes =
        Arrays.stream(names)
            .map(
                name -> {
                  Theme theme = org.springframework.beans.BeanUtils.instantiateClass(Theme.class);
                  ReflectionTestUtils.setField(theme, "name", name);
                  return theme;
                })
            .toList();
    Course course =
        Course.createDraft(
            "owner", "코스", CourseTransport.CAR, null, null, themes, LocalDateTime.now(CLOCK));
    ReflectionTestUtils.setField(course, "courseId", id);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);
    return course;
  }
}

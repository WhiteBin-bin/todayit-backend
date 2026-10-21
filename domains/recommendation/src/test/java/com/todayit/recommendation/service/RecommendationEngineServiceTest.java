package com.todayit.recommendation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.todayit.recommendation.model.CourseCandidate;
import com.todayit.recommendation.model.RecommendationContext;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecommendationEngineServiceTest {

  private final RecommendationEngineService service = new RecommendationEngineService();

  @Test
  @DisplayName("기획서 검수 기준: R=0.75, C=1.0, P=0.5, H=0.6일 때 B=0.9이고 최종 점수는 정확히 75점이 된다")
  void shouldMatchSpecificationExampleScore75() {
    // given
    RecommendationContext context =
        new RecommendationContext(
            "member-1", true, "1111051500", Set.of("HEALING", "NATURE"), true);

    Instant publishedAt = Instant.parse("2026-09-20T00:00:00Z");
    CourseCandidate candidate =
        new CourseCandidate(
            "course-1", true, false, 4, 3, Set.of("HEALING", "NATURE"), 50.0, publishedAt);
    candidate.setHistoryScore(0.6);

    CourseCandidate benchmarkCandidate =
        new CourseCandidate("course-max", true, false, 2, 2, Set.of("HEALING"), 100.0, publishedAt);

    // when
    List<CourseCandidate> ranked =
        service.evaluateAndRank(context, List.of(candidate, benchmarkCandidate));

    // then
    CourseCandidate target =
        ranked.stream().filter(c -> c.getCourseId().equals("course-1")).findFirst().orElseThrow();

    assertEquals(0.75, target.getRegionScore(), 0.001);
    assertEquals(1.0, target.getConceptScore(), 0.001);
    assertEquals(0.5, target.getPopularityNormalizedScore(), 0.001);
    assertEquals(0.9, target.getBaseScore(), 0.001);
    assertEquals(75.0, target.getFinalScore(), 0.001);
  }

  @Test
  @DisplayName("선호지역(R)이 누락되면 기본점수(B) 50%와 개인활동(H) 50%로 재정규화된다")
  void shouldRenormalizeWhenRegionIsMissing() {
    // given
    RecommendationContext context =
        new RecommendationContext("member-1", true, null, Set.of("CAFE"), true);

    CourseCandidate candidate =
        new CourseCandidate("course-1", true, false, 3, 0, Set.of("CAFE"), 100.0, Instant.now());
    candidate.setHistoryScore(0.8);

    // when
    List<CourseCandidate> ranked = service.evaluateAndRank(context, List.of(candidate));

    // then
    assertEquals(1, ranked.size());
    assertEquals(90.0, ranked.get(0).getFinalScore(), 0.001);
  }

  @Test
  @DisplayName("개인화 동의를 철회한 경우 추천을 중단하고 인기 원점수 기준으로 대체 전환된다")
  void shouldFallbackToPopularWhenConsentRevoked() {
    // given
    RecommendationContext context =
        new RecommendationContext("member-1", false, "1111051500", Set.of("CAFE"), true);

    CourseCandidate candidateLowPopular =
        new CourseCandidate("course-low", true, false, 4, 4, Set.of("CAFE"), 20.0, Instant.now());
    CourseCandidate candidateHighPopular =
        new CourseCandidate("course-high", true, false, 4, 0, Set.of(), 80.0, Instant.now());

    // when
    List<CourseCandidate> ranked =
        service.evaluateAndRank(context, List.of(candidateLowPopular, candidateHighPopular));

    // then
    assertEquals(2, ranked.size());
    assertTrue(ranked.get(0).isFallbackToPopular());
    assertEquals("course-high", ranked.get(0).getCourseId());
    assertEquals(80.0, ranked.get(0).getFinalScore());
    assertEquals("course-low", ranked.get(1).getCourseId());
    assertEquals(20.0, ranked.get(1).getFinalScore());
  }

  @Test
  @DisplayName("비공개이거나 삭제된 코스는 적격성(eligible)이 false가 되어 결과에서 제외된다")
  void shouldExcludeInvisibleOrDeletedCourses() {
    // given
    RecommendationContext context =
        new RecommendationContext("member-1", true, "1111051500", Set.of("CAFE"), true);

    CourseCandidate normalCourse =
        new CourseCandidate(
            "course-normal", true, false, 2, 2, Set.of("CAFE"), 10.0, Instant.now());
    CourseCandidate privateCourse =
        new CourseCandidate(
            "course-private", false, false, 2, 2, Set.of("CAFE"), 50.0, Instant.now());
    CourseCandidate deletedCourse =
        new CourseCandidate(
            "course-deleted", true, true, 2, 2, Set.of("CAFE"), 50.0, Instant.now());

    // when
    List<CourseCandidate> ranked =
        service.evaluateAndRank(context, List.of(normalCourse, privateCourse, deletedCourse));

    // then
    assertEquals(1, ranked.size());
    assertEquals("course-normal", ranked.get(0).getCourseId());
    assertFalse(privateCourse.isEligible());
    assertFalse(deletedCourse.isEligible());
  }

  @Test
  @DisplayName("점수 동점일 경우 코스 공개일 내림차순, 공개일 누락은 후순위, 그다음 ID 오름차순으로 정렬된다")
  void shouldApplyTieBreakerWithPublishedDateAndId() {
    // given
    RecommendationContext context =
        new RecommendationContext("member-1", false, null, Set.of(), false);

    Instant recent = Instant.parse("2026-09-20T00:00:00Z");
    Instant old = Instant.parse("2026-09-10T00:00:00Z");

    CourseCandidate courseOld =
        new CourseCandidate("course-B", true, false, 1, 0, Set.of(), 50.0, old);
    CourseCandidate courseRecentB =
        new CourseCandidate("course-recent-2", true, false, 1, 0, Set.of(), 50.0, recent);
    CourseCandidate courseRecentA =
        new CourseCandidate("course-recent-1", true, false, 1, 0, Set.of(), 50.0, recent);
    CourseCandidate courseNullDate =
        new CourseCandidate("course-null", true, false, 1, 0, Set.of(), 50.0, null);

    // when
    List<CourseCandidate> ranked =
        service.evaluateAndRank(
            context, List.of(courseOld, courseNullDate, courseRecentB, courseRecentA));

    // then
    assertEquals(
        List.of("course-recent-1", "course-recent-2", "course-B", "course-null"),
        ranked.stream().map(CourseCandidate::getCourseId).toList());
  }

  @Test
  @DisplayName("일치 장소 수가 전체 장소 수를 초과하거나 음수이면 IllegalArgumentException을 던진다")
  void shouldRejectInvalidPlaceCounts() {
    // then
    assertThrows(
        IllegalArgumentException.class,
        () -> new CourseCandidate("course-err-1", true, false, 1, 2, Set.of(), 0.0, Instant.now()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new CourseCandidate("course-err-2", true, false, -1, 0, Set.of(), 0.0, Instant.now()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new CourseCandidate("course-err-3", true, false, 2, -1, Set.of(), 0.0, Instant.now()));
  }
}

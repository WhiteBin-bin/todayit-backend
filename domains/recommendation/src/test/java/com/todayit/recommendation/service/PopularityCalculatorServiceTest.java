package com.todayit.recommendation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.todayit.recommendation.model.PopularityCandidate;
import com.todayit.recommendation.model.PopularityTargetType;
import com.todayit.recommendation.model.PopularityWindow;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularityCalculatorServiceTest {

  private final PopularityCalculatorService service = new PopularityCalculatorService();

  @Test
  @DisplayName("인기 원점수 공식(L*1 + S*2 + V*0.1)에 따라 A(50점)가 B(46점)보다 먼저 정렬된다")
  void shouldCalculatePopularityScoreAndRankDescending() {
    // given
    Instant publishedAt = Instant.parse("2026-09-20T00:00:00Z");
    PopularityCandidate candidateB =
        new PopularityCandidate(
            PopularityTargetType.COURSE, "course-B", true, false, 30, 3, 100, publishedAt);
    PopularityCandidate candidateA =
        new PopularityCandidate(
            PopularityTargetType.COURSE, "course-A", true, false, 20, 10, 100, publishedAt);

    // when
    List<PopularityCandidate> ranked =
        service.evaluateAndRank(PopularityTargetType.COURSE, List.of(candidateB, candidateA));

    // then
    assertEquals(2, ranked.size());
    assertEquals("course-A", ranked.get(0).getTargetId());
    assertEquals(50.0, ranked.get(0).getPopularityScore());
    assertEquals("course-B", ranked.get(1).getTargetId());
    assertEquals(46.0, ranked.get(1).getPopularityScore());
  }

  @Test
  @DisplayName("비공개·임시저장·삭제된 대상과 다른 대상 유형(코스/장소)은 인기 목록에서 제외된다")
  void shouldExcludeInvisibleDeletedOrDifferentTargetType() {
    // given
    Instant now = Instant.parse("2026-09-20T00:00:00Z");
    PopularityCandidate validCourse =
        new PopularityCandidate(
            PopularityTargetType.COURSE, "course-valid", true, false, 0, 0, 0, now);
    PopularityCandidate privateCourse =
        new PopularityCandidate(
            PopularityTargetType.COURSE, "course-private", false, false, 100, 50, 500, now);
    PopularityCandidate deletedCourse =
        new PopularityCandidate(
            PopularityTargetType.COURSE, "course-deleted", true, true, 100, 50, 500, now);
    PopularityCandidate validPlace =
        new PopularityCandidate(
            PopularityTargetType.PLACE, "place-valid", true, false, 100, 50, 500, now);

    // when
    List<PopularityCandidate> ranked =
        service.evaluateAndRank(
            PopularityTargetType.COURSE,
            List.of(validCourse, privateCourse, deletedCourse, validPlace));

    // then
    assertEquals(1, ranked.size());
    assertEquals("course-valid", ranked.get(0).getTargetId());
    assertEquals(0.0, ranked.get(0).getPopularityScore());
    assertFalse(privateCourse.isEligible());
    assertFalse(deletedCourse.isEligible());
  }

  @Test
  @DisplayName("동점일 때 최근 노출일 내림차순, 날짜 누락(null)은 뒤로, 그다음 ID 오름차순으로 정렬된다")
  void shouldApplyTieBreakerWithNullDatesLastAndIdAscending() {
    // given
    PopularityCandidate placeOld =
        new PopularityCandidate(
            PopularityTargetType.PLACE,
            "place-2",
            true,
            false,
            10,
            0,
            0,
            Instant.parse("2026-09-10T00:00:00Z"));
    PopularityCandidate placeRecentB =
        new PopularityCandidate(
            PopularityTargetType.PLACE,
            "place-B",
            true,
            false,
            10,
            0,
            0,
            Instant.parse("2026-09-15T00:00:00Z"));
    PopularityCandidate placeRecentA =
        new PopularityCandidate(
            PopularityTargetType.PLACE,
            "place-A",
            true,
            false,
            10,
            0,
            0,
            Instant.parse("2026-09-15T00:00:00Z"));
    PopularityCandidate placeNullDate2 =
        new PopularityCandidate(
            PopularityTargetType.PLACE, "place-null-2", true, false, 10, 0, 0, null);
    PopularityCandidate placeNullDate1 =
        new PopularityCandidate(
            PopularityTargetType.PLACE, "place-null-1", true, false, 10, 0, 0, null);

    // when
    List<PopularityCandidate> ranked =
        service.evaluateAndRank(
            PopularityTargetType.PLACE,
            List.of(placeNullDate2, placeOld, placeRecentB, placeNullDate1, placeRecentA));

    // then
    assertEquals(
        List.of("place-A", "place-B", "place-2", "place-null-1", "place-null-2"),
        ranked.stream().map(PopularityCandidate::getTargetId).toList());
  }

  @Test
  @DisplayName("KST 자정 기준 당일 포함 최근 14일 구간과 스크랩 활성 여부를 정확히 판별한다")
  void shouldVerifyFourteenDayKstMidnightWindowAndReactionStatus() {
    // given: 2026-09-22 12:10 KST 기준 -> 2026-09-09 00:00 KST부터 유효
    Instant referenceTime =
        ZonedDateTime.of(2026, 9, 22, 12, 10, 0, 0, PopularityWindow.KST_ZONE).toInstant();
    PopularityWindow window = new PopularityWindow(referenceTime);

    Instant boundaryStart =
        ZonedDateTime.of(2026, 9, 9, 0, 0, 0, 0, PopularityWindow.KST_ZONE).toInstant();
    Instant beforeStart = boundaryStart.minusSeconds(1);

    // then
    assertEquals(boundaryStart, window.getStartInclusive());
    assertTrue(window.isValidReaction(boundaryStart, true));
    assertFalse(window.isValidReaction(beforeStart, true));
    assertFalse(window.isValidReaction(boundaryStart, false));
    assertFalse(window.isValidReaction(referenceTime, true));
  }

  @Test
  @DisplayName("정상 집계가 없을 때 대체 목록은 공개일/최초 노출일 내림차순과 ID 오름차순으로 정렬한다")
  void shouldProvideFallbackOrderWhenNoNormalAggregationExists() {
    // given
    PopularityCandidate olderButHigherScore =
        new PopularityCandidate(
            PopularityTargetType.COURSE,
            "course-1",
            true,
            false,
            50,
            20,
            100,
            Instant.parse("2026-09-01T00:00:00Z"));
    PopularityCandidate newerButZeroScore =
        new PopularityCandidate(
            PopularityTargetType.COURSE,
            "course-2",
            true,
            false,
            0,
            0,
            0,
            Instant.parse("2026-09-20T00:00:00Z"));

    // when
    List<PopularityCandidate> fallbackList =
        service.rankByFallbackOrder(
            PopularityTargetType.COURSE, List.of(olderButHigherScore, newerButZeroScore));

    // then
    assertEquals(
        List.of("course-2", "course-1"),
        fallbackList.stream().map(PopularityCandidate::getTargetId).toList());
  }
}

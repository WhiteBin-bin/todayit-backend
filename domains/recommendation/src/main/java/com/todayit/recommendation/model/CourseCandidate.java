package com.todayit.recommendation.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 추천 점수 계산 및 자격 평가 대상 코스 팩트 모델입니다.
 *
 * <p>지역 점수 R, 콘셉트 점수 C, 정규화 인기 점수 P, 개인 활동 점수 H를 바탕으로 최종 추천 점수를 계산합니다.
 */
public class CourseCandidate {

  private final String courseId;
  private final boolean visible;
  private final boolean deleted;
  private final int totalPlaceCount;
  private final int matchingDongPlaceCount;
  private final Set<String> concepts;
  private final double popularityRawScore;
  private final Instant publishedAt;

  private double regionScore;
  private double conceptScore;
  private double popularityNormalizedScore;
  private double historyScore;

  private boolean eligible;
  private boolean fallbackToPopular;
  private double finalScore;

  /**
   * 추천 후보 코스를 생성합니다.
   *
   * @param courseId 코스 식별자 (null 불가)
   * @param visible 공개·확정 여부
   * @param deleted 삭제 여부
   * @param totalPlaceCount 코스에 포함된 전체 장소 수 (0 이상)
   * @param matchingDongPlaceCount 선호 동에 포함된 장소 수 (0 이상, totalPlaceCount 이하)
   * @param concepts 코스 콘셉트 목록 (null 허용, 빈 Set 처리)
   * @param popularityRawScore 최근 14일 인기 원점수 (0 이상)
   * @param publishedAt 코스 공개일 (null 허용)
   * @throws NullPointerException courseId가 null인 경우
   * @throws IllegalArgumentException 장소 수가 음수이거나 일치 장소 수가 전체 장소 수를 초과하는 경우
   */
  public CourseCandidate(
      String courseId,
      boolean visible,
      boolean deleted,
      int totalPlaceCount,
      int matchingDongPlaceCount,
      Set<String> concepts,
      double popularityRawScore,
      Instant publishedAt) {
    this.courseId = Objects.requireNonNull(courseId, "코스 식별자는 필수입니다.");
    if (totalPlaceCount < 0) {
      throw new IllegalArgumentException("전체 장소 수는 음수일 수 없습니다.");
    }
    if (matchingDongPlaceCount < 0) {
      throw new IllegalArgumentException("일치 장소 수는 음수일 수 없습니다.");
    }
    if (matchingDongPlaceCount > totalPlaceCount) {
      throw new IllegalArgumentException("일치 장소 수는 전체 장소 수를 초과할 수 없습니다.");
    }

    this.visible = visible;
    this.deleted = deleted;
    this.totalPlaceCount = totalPlaceCount;
    this.matchingDongPlaceCount = matchingDongPlaceCount;
    this.concepts =
        concepts == null
            ? Collections.emptySet()
            : Collections.unmodifiableSet(new HashSet<>(concepts));
    this.popularityRawScore = Math.max(0.0, popularityRawScore);
    this.publishedAt = publishedAt;

    this.regionScore =
        this.totalPlaceCount > 0
            ? (double) this.matchingDongPlaceCount / this.totalPlaceCount
            : 0.0;
    this.conceptScore = 0.0;
    this.popularityNormalizedScore = 0.0;
    this.historyScore = 0.0;
    this.eligible = false;
    this.fallbackToPopular = false;
    this.finalScore = 0.0;
  }

  /**
   * 요청 콘셉트와 코스 콘셉트 간의 일치 비율 C (0.0 ~ 1.0)를 계산하여 설정합니다.
   *
   * @param selectedConcepts 사용자가 요청에서 선택한 콘셉트 목록
   */
  public void calculateConceptScore(Set<String> selectedConcepts) {
    if (selectedConcepts == null || selectedConcepts.isEmpty()) {
      this.conceptScore = 0.0;
      return;
    }
    long matchCount = selectedConcepts.stream().filter(concepts::contains).count();
    this.conceptScore = (double) matchCount / selectedConcepts.size();
  }

  /**
   * 후보군 최대 인기 원점수를 기준으로 정규화 인기 점수 P (0.0 ~ 1.0)를 설정합니다.
   *
   * @param maxPopularityScore 후보군 중 가장 높은 인기 원점수
   */
  public void normalizePopularityScore(double maxPopularityScore) {
    if (maxPopularityScore <= 0.0) {
      this.popularityNormalizedScore = 0.0;
    } else {
      this.popularityNormalizedScore = Math.min(1.0, this.popularityRawScore / maxPopularityScore);
    }
  }

  /**
   * 기본 점수 B (0.8 * C + 0.2 * P)를 계산하여 반환합니다.
   *
   * @return 기본 점수 (0.0 ~ 1.0)
   */
  public double getBaseScore() {
    return 0.8 * conceptScore + 0.2 * popularityNormalizedScore;
  }

  /**
   * 부동소수점 오차 없이 지정된 가중치로 최종 점수를 0~100 범위로 설정합니다.
   *
   * @param regionWeight 지역 가중치 (0.0 ~ 1.0)
   * @param baseWeight 기본점수 가중치 (0.0 ~ 1.0)
   * @param historyWeight 개인행동 가중치 (0.0 ~ 1.0)
   */
  public void applyFinalScore(double regionWeight, double baseWeight, double historyWeight) {
    BigDecimal r = BigDecimal.valueOf(regionScore).multiply(BigDecimal.valueOf(regionWeight));
    BigDecimal b = BigDecimal.valueOf(getBaseScore()).multiply(BigDecimal.valueOf(baseWeight));
    BigDecimal h = BigDecimal.valueOf(historyScore).multiply(BigDecimal.valueOf(historyWeight));

    BigDecimal combined = r.add(b).add(h).multiply(BigDecimal.valueOf(100));
    this.finalScore = combined.setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * 코스 식별자를 반환합니다.
   *
   * @return 코스 ID
   */
  public String getCourseId() {
    return courseId;
  }

  /**
   * 공개·확정 여부를 반환합니다.
   *
   * @return 공개 상태이면 true
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * 삭제 여부를 반환합니다.
   *
   * @return 삭제되었으면 true
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * 코스 전체 장소 수를 반환합니다.
   *
   * @return 전체 장소 수
   */
  public int getTotalPlaceCount() {
    return totalPlaceCount;
  }

  /**
   * 선호 동에 포함된 장소 수를 반환합니다.
   *
   * @return 선호 동 장소 수
   */
  public int getMatchingDongPlaceCount() {
    return matchingDongPlaceCount;
  }

  /**
   * 코스 콘셉트 목록을 반환합니다.
   *
   * @return 코스 콘셉트 Set
   */
  public Set<String> getConcepts() {
    return concepts;
  }

  /**
   * 최근 14일 인기 원점수를 반환합니다.
   *
   * @return 인기 원점수
   */
  public double getPopularityRawScore() {
    return popularityRawScore;
  }

  /**
   * 코스 공개일을 반환합니다.
   *
   * @return 코스 공개일
   */
  public Instant getPublishedAt() {
    return publishedAt;
  }

  /**
   * 지역 일치도 점수 R을 반환합니다.
   *
   * @return 지역 일치도 점수 (0.0 ~ 1.0)
   */
  public double getRegionScore() {
    return regionScore;
  }

  /**
   * 지역 일치도 점수 R을 설정합니다.
   *
   * @param regionScore 지역 일치도 점수
   */
  public void setRegionScore(double regionScore) {
    this.regionScore = regionScore;
  }

  /**
   * 요청 콘셉트 일치도 C를 반환합니다.
   *
   * @return 콘셉트 점수 (0.0 ~ 1.0)
   */
  public double getConceptScore() {
    return conceptScore;
  }

  /**
   * 콘셉트 일치도 C를 직접 설정합니다.
   *
   * @param conceptScore 콘셉트 점수
   */
  public void setConceptScore(double conceptScore) {
    this.conceptScore = conceptScore;
  }

  /**
   * 정규화된 인기 점수 P를 반환합니다.
   *
   * @return 정규화 인기 점수 (0.0 ~ 1.0)
   */
  public double getPopularityNormalizedScore() {
    return popularityNormalizedScore;
  }

  /**
   * 정규화된 인기 점수 P를 직접 설정합니다.
   *
   * @param popularityNormalizedScore 정규화 인기 점수
   */
  public void setPopularityNormalizedScore(double popularityNormalizedScore) {
    this.popularityNormalizedScore = popularityNormalizedScore;
  }

  /**
   * 개인 행동 유사도 점수 H를 반환합니다.
   *
   * @return 개인 행동 점수 (0.0 ~ 1.0)
   */
  public double getHistoryScore() {
    return historyScore;
  }

  /**
   * 개인 행동 유사도 점수 H를 설정합니다.
   *
   * @param historyScore 개인 행동 점수
   */
  public void setHistoryScore(double historyScore) {
    this.historyScore = historyScore;
  }

  /**
   * 추천 목록 노출 자격 여부를 반환합니다.
   *
   * @return 노출 가능 여부
   */
  public boolean isEligible() {
    return eligible;
  }

  /**
   * 추천 목록 노출 자격 여부를 설정합니다.
   *
   * @param eligible 노출 가능 여부
   */
  public void setEligible(boolean eligible) {
    this.eligible = eligible;
  }

  /**
   * 인기순 대체 목록으로 전환되었는지 여부를 반환합니다.
   *
   * @return 인기순 대체 여부
   */
  public boolean isFallbackToPopular() {
    return fallbackToPopular;
  }

  /**
   * 인기순 대체 목록 전환 여부를 설정합니다.
   *
   * @param fallbackToPopular 인기순 대체 여부
   */
  public void setFallbackToPopular(boolean fallbackToPopular) {
    this.fallbackToPopular = fallbackToPopular;
  }

  /**
   * 최종 계산된 추천 점수를 반환합니다.
   *
   * @return 최종 추천 점수
   */
  public double getFinalScore() {
    return finalScore;
  }

  /**
   * 최종 계산된 추천 점수를 직접 설정합니다.
   *
   * @param finalScore 최종 추천 점수
   */
  public void setFinalScore(double finalScore) {
    this.finalScore = finalScore;
  }
}

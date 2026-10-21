package com.todayit.recommendation.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * 인기순 점수 계산 및 자격 검증 대상(코스 또는 장소) 팩트 모델입니다.
 *
 * <p>인기 원점수 공식: {@code 좋아요 유효 사용자 수(L) × 1 + 스크랩 유효 사용자 수(S) × 2 + 유효 조회수(V) × 0.1}
 */
public class PopularityCandidate {

  private final PopularityTargetType targetType;
  private final String targetId;
  private final boolean visible;
  private final boolean deleted;
  private final long likeCount;
  private final long scrapCount;
  private final long validViewCount;
  private final Instant exposedAt;

  private boolean eligible;
  private double popularityScore;

  /**
   * 인기순 평가 대상을 생성합니다.
   *
   * @param targetType 대상 유형(COURSE 또는 PLACE, null 불가)
   * @param targetId 대상 식별자(null 불가)
   * @param visible 공개·확정 코스 또는 노출 가능 장소 여부
   * @param deleted 삭제 여부
   * @param likeCount 기간 내 활성 좋아요 유효 사용자 수(L, 0 이상)
   * @param scrapCount 기간 내 활성 스크랩 유효 사용자 수(S, 0 이상)
   * @param validViewCount 기간 내 유효 조회수(V, 0 이상)
   * @param exposedAt 코스 공개일 또는 장소 최초 노출일(장소 날짜 누락 시 null 허용)
   * @throws NullPointerException targetType 또는 targetId가 null인 경우
   * @throws IllegalArgumentException 반응 수(L, S, V)가 음수인 경우
   */
  public PopularityCandidate(
      PopularityTargetType targetType,
      String targetId,
      boolean visible,
      boolean deleted,
      long likeCount,
      long scrapCount,
      long validViewCount,
      Instant exposedAt) {
    this.targetType = Objects.requireNonNull(targetType, "대상 유형은 필수입니다.");
    this.targetId = Objects.requireNonNull(targetId, "대상 식별자는 필수입니다.");
    if (likeCount < 0 || scrapCount < 0 || validViewCount < 0) {
      throw new IllegalArgumentException("반응 수 및 조회수는 음수일 수 없습니다.");
    }
    this.visible = visible;
    this.deleted = deleted;
    this.likeCount = likeCount;
    this.scrapCount = scrapCount;
    this.validViewCount = validViewCount;
    this.exposedAt = exposedAt;
    this.eligible = false;
    this.popularityScore = 0.0;
  }

  /**
   * 가중치 공식에 따라 부동소수점 오차 없이 인기 원점수를 계산하여 반영합니다.
   *
   * @param likeWeight 좋아요 가중치(기본 1.0)
   * @param scrapWeight 스크랩 가중치(기본 2.0)
   * @param viewWeight 조회 가중치(기본 0.1)
   */
  public void applyWeightedScore(double likeWeight, double scrapWeight, double viewWeight) {
    BigDecimal likes = BigDecimal.valueOf(likeCount).multiply(BigDecimal.valueOf(likeWeight));
    BigDecimal scraps = BigDecimal.valueOf(scrapCount).multiply(BigDecimal.valueOf(scrapWeight));
    BigDecimal views = BigDecimal.valueOf(validViewCount).multiply(BigDecimal.valueOf(viewWeight));
    this.popularityScore =
        likes.add(scraps).add(views).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  /**
   * 대상 유형을 반환합니다.
   *
   * @return 대상 유형(COURSE 또는 PLACE)
   */
  public PopularityTargetType getTargetType() {
    return targetType;
  }

  /**
   * 대상 식별자를 반환합니다.
   *
   * @return 대상 식별자
   */
  public String getTargetId() {
    return targetId;
  }

  /**
   * 공개·확정(코스) 또는 서비스 노출 가능(장소) 여부를 반환합니다.
   *
   * @return 노출 가능 여부
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * 삭제 여부를 반환합니다.
   *
   * @return 삭제 여부
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * 기간 내 유효 좋아요 수(L)를 반환합니다.
   *
   * @return 유효 좋아요 수
   */
  public long getLikeCount() {
    return likeCount;
  }

  /**
   * 기간 내 유효 스크랩 수(S)를 반환합니다.
   *
   * @return 유효 스크랩 수
   */
  public long getScrapCount() {
    return scrapCount;
  }

  /**
   * 기간 내 유효 조회수(V)를 반환합니다.
   *
   * @return 유효 조회수
   */
  public long getValidViewCount() {
    return validViewCount;
  }

  /**
   * 코스 공개일 또는 장소 최초 노출일을 반환합니다.
   *
   * @return 공개일/최초 노출일(누락 시 null)
   */
  public Instant getExposedAt() {
    return exposedAt;
  }

  /**
   * 인기 목록 노출 자격 여부를 반환합니다.
   *
   * @return 노출 자격 여부
   */
  public boolean isEligible() {
    return eligible;
  }

  /**
   * 인기 목록 노출 자격 여부를 설정합니다.
   *
   * @param eligible 노출 자격 여부
   */
  public void setEligible(boolean eligible) {
    this.eligible = eligible;
  }

  /**
   * 계산된 인기 원점수를 반환합니다.
   *
   * @return 인기 원점수
   */
  public double getPopularityScore() {
    return popularityScore;
  }

  /**
   * 인기 원점수를 직접 설정합니다.
   *
   * @param popularityScore 인기 원점수
   */
  public void setPopularityScore(double popularityScore) {
    this.popularityScore = popularityScore;
  }
}

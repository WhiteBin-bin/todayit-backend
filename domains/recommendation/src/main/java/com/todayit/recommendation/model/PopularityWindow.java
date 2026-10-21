package com.todayit.recommendation.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 한국 시간(KST) 자정 기준 당일 포함 최근 14일 인기 집계 구간을 관리합니다.
 *
 * <p>예를 들어 집계 기준시각이 9월 22일 KST라면 집계 시작 시각은 9월 9일 00:00 KST(포함)부터 집계 기준시각 T(제외)까지입니다.
 */
public class PopularityWindow {

  /** 인기 집계 기준 시간대(KST). */
  public static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  /** 당일 포함 집계 일수(14일: 당일 + 이전 13일). */
  public static final int WINDOW_DAYS = 14;

  private final Instant startInclusive;
  private final Instant endExclusive;

  /**
   * 집계 기준시각을 바탕으로 당일 포함 최근 14일(KST 자정 기준) 구간을 생성합니다.
   *
   * @param referenceTime 집계 기준시각 T (null 불가)
   * @throws NullPointerException referenceTime이 null인 경우
   */
  public PopularityWindow(Instant referenceTime) {
    Objects.requireNonNull(referenceTime, "집계 기준시각은 필수입니다.");
    LocalDate referenceDateKst = LocalDate.ofInstant(referenceTime, KST_ZONE);
    LocalDate startDateKst = referenceDateKst.minusDays(WINDOW_DAYS - 1L);
    this.startInclusive = startDateKst.atStartOfDay(KST_ZONE).toInstant();
    this.endExclusive = referenceTime;
  }

  /**
   * 이벤트 발생 시각이 집계 기간(시작 시각 이상, 기준시각 미만) 내에 속하는지 확인합니다.
   *
   * @param eventTime 반응 최초 생성시각 또는 조회 발생 시각
   * @return 기간 내에 속하면 true, 아니면 false
   */
  public boolean isWithinWindow(Instant eventTime) {
    if (eventTime == null) {
      return false;
    }
    return !eventTime.isBefore(startInclusive) && eventTime.isBefore(endExclusive);
  }

  /**
   * 좋아요·스크랩 반응이 집계 대상(집계 기간 내 최초 생성 및 현재 활성 상태)인지 판별합니다.
   *
   * @param firstCreatedAt 최초 반응 생성시각(스크랩 재등록 시에도 최초 시각 유지)
   * @param active 집계 시점 활성 여부(스크랩 취소 시 false)
   * @return 유효한 반응이면 true, 아니면 false
   */
  public boolean isValidReaction(Instant firstCreatedAt, boolean active) {
    return active && isWithinWindow(firstCreatedAt);
  }

  /**
   * 집계 시작 시각(KST 14일 전 자정, 포함)을 반환합니다.
   *
   * @return 집계 시작 시각
   */
  public Instant getStartInclusive() {
    return startInclusive;
  }

  /**
   * 집계 종료 기준시각(제외)을 반환합니다.
   *
   * @return 집계 기준시각
   */
  public Instant getEndExclusive() {
    return endExclusive;
  }
}

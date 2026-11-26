package com.todayit.course.service.model;

/**
 * 코스 목록 및 필터 조회의 소요 시간 구간 필터입니다.
 *
 * <ul>
 *   <li>{@link #ALL}: 전체 (시간 필터 미적용)
 *   <li>{@link #UNDER_2_HOURS}: 2시간 이하 (120분 이하)
 *   <li>{@link #TWO_TO_FOUR_HOURS}: 2~4시간 (120분 초과 ~ 240분 이하)
 *   <li>{@link #OVER_4_HOURS}: 4시간 초과 (240분 초과)
 * </ul>
 */
public enum CourseDurationFilter {
  ALL(null, null),
  UNDER_2_HOURS(null, 120),
  TWO_TO_FOUR_HOURS(120, 240),
  OVER_4_HOURS(240, null);

  private final Integer minExclusiveMinutes;
  private final Integer maxInclusiveMinutes;

  CourseDurationFilter(Integer minExclusiveMinutes, Integer maxInclusiveMinutes) {
    this.minExclusiveMinutes = minExclusiveMinutes;
    this.maxInclusiveMinutes = maxInclusiveMinutes;
  }

  /**
   * 구간의 하한(초과, 분)을 반환합니다.
   *
   * @return 하한(분, 초과), 하한이 없으면 null
   */
  public Integer minExclusiveMinutes() {
    return minExclusiveMinutes;
  }

  /**
   * 구간의 상한(이하, 분)을 반환합니다.
   *
   * @return 상한(분, 이하), 상한이 없으면 null
   */
  public Integer maxInclusiveMinutes() {
    return maxInclusiveMinutes;
  }
}

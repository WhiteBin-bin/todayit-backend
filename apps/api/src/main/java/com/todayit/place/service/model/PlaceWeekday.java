package com.todayit.place.service.model;

/** 장소 영업일 필터에 사용하는 요일입니다. */
public enum PlaceWeekday {
  /** 월요일입니다. */
  MONDAY(1),

  /** 화요일입니다. */
  TUESDAY(2),

  /** 수요일입니다. */
  WEDNESDAY(3),

  /** 목요일입니다. */
  THURSDAY(4),

  /** 금요일입니다. */
  FRIDAY(5),

  /** 토요일입니다. */
  SATURDAY(6),

  /** 일요일입니다. */
  SUNDAY(7);

  /** ISO-8601에서 정의한 요일 값입니다. */
  private final int isoValue;

  /**
   * 장소 영업일 필터 요일을 생성합니다.
   *
   * @param isoValue 월요일 1부터 일요일 7까지의 ISO-8601 요일 값
   */
  PlaceWeekday(int isoValue) {
    this.isoValue = isoValue;
  }

  /**
   * ISO-8601 요일 값을 반환합니다.
   *
   * @return 월요일 1부터 일요일 7까지의 값
   */
  public int getIsoValue() {
    return isoValue;
  }
}

package com.todayit.place.exception;

/** 장소 영업 요일 필터 값이 올바르지 않을 때 발생하는 예외입니다. */
public class InvalidPlaceWeekdayFilterException extends PlaceException {

  /** 잘못된 장소 영업 요일 필터 오류를 생성합니다. */
  public InvalidPlaceWeekdayFilterException() {
    super(PlaceErrorCode.INVALID_WEEKDAY_FILTER);
  }
}

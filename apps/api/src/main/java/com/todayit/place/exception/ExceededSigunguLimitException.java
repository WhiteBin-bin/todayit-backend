package com.todayit.place.exception;

/** 끝자리가 0인 시군구 코드가 시도당 3개를 초과했을 때 발생하는 예외입니다. */
public class ExceededSigunguLimitException extends PlaceException {

  /** 시군구 필터 개수 초과 오류를 생성합니다. */
  public ExceededSigunguLimitException() {
    super(PlaceErrorCode.EXCEEDED_SIGUNGU_LIMIT);
  }
}

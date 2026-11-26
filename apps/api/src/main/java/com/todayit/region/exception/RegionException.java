package com.todayit.region.exception;

import com.todayit.common.exception.BusinessException;

/** 지역 기능의 업무 규칙 위반을 오류 코드로 구분하는 예외입니다. */
public class RegionException extends BusinessException {

  /**
   * 지역 오류 코드에 해당하는 예외를 생성합니다.
   *
   * @param errorCode 지역 오류 코드
   */
  public RegionException(RegionErrorCode errorCode) {
    super(errorCode);
  }
}

package com.todayit.course.exception;

import com.todayit.common.exception.BusinessException;

/** 요청한 지역의 상하위 경로가 존재하지 않을 때 발생하는 예외입니다. */
public class InvalidRegionPathException extends BusinessException {

  /** 잘못된 지역 경로 오류를 생성합니다. */
  public InvalidRegionPathException() {
    super(RegionErrorCode.INVALID_PATH);
  }
}

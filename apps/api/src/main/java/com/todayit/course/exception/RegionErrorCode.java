package com.todayit.course.exception;

import com.todayit.common.exception.ErrorCode;
import com.todayit.common.exception.ErrorStatus;

/** 지역 기능에서 사용하는 오류 코드입니다. */
public enum RegionErrorCode implements ErrorCode {
  /** 요청한 지역의 상하위 경로가 존재하지 않는 경우입니다. */
  INVALID_PATH("INVALID_REGION_PATH", "지역 필터 값이 올바르지 않습니다.", ErrorStatus.BAD_REQUEST);

  /** 클라이언트에 전달할 오류 코드입니다. */
  private final String code;

  /** 클라이언트에 전달할 오류 메시지입니다. */
  private final String message;

  /** 오류를 변환할 HTTP 상태입니다. */
  private final ErrorStatus status;

  /**
   * 지역 오류 코드를 생성합니다.
   *
   * @param code 클라이언트에 전달할 오류 코드
   * @param message 클라이언트에 전달할 오류 메시지
   * @param status 오류를 변환할 HTTP 상태
   */
  RegionErrorCode(String code, String message, ErrorStatus status) {
    this.code = code;
    this.message = message;
    this.status = status;
  }

  /** {@inheritDoc} */
  @Override
  public String code() {
    return code;
  }

  /** {@inheritDoc} */
  @Override
  public String message() {
    return message;
  }

  /** {@inheritDoc} */
  @Override
  public ErrorStatus status() {
    return status;
  }
}

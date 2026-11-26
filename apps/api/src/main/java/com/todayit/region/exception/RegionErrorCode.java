package com.todayit.region.exception;

import com.todayit.common.exception.ErrorCode;
import com.todayit.common.exception.ErrorStatus;

/** 지역 기능에서 사용하는 오류 코드입니다. */
public enum RegionErrorCode implements ErrorCode {
  /** 요청한 지역의 상하위 경로가 존재하지 않는 경우입니다. */
  INVALID_PATH("INVALID_REGION_PATH", "지역 필터 값이 올바르지 않습니다.", ErrorStatus.BAD_REQUEST),

  /** 끝자리가 0인 시군구 코드 개수가 시도당 최대 개수를 초과한 경우입니다. */
  EXCEEDED_SIGUNGU_LIMIT(
      "EXCEEDED_SIGUNGU_LIMIT", "끝자리가 0인 시군구 코드는 시도당 최대 3개까지 선택할 수 있습니다.", ErrorStatus.BAD_REQUEST);

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

  /**
   * 지역 오류의 외부 공개 코드를 반환합니다.
   *
   * @return 오류 코드
   */
  @Override
  public String code() {
    return code;
  }

  /**
   * 지역 오류의 안내 메시지를 반환합니다.
   *
   * @return 오류 메시지
   */
  @Override
  public String message() {
    return message;
  }

  /**
   * 지역 오류에 대응하는 응답 상태를 반환합니다.
   *
   * @return 업무 오류 상태
   */
  @Override
  public ErrorStatus status() {
    return status;
  }
}

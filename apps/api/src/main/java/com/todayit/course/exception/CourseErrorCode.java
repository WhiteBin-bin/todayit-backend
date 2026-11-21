package com.todayit.course.exception;

import com.todayit.common.exception.ErrorCode;
import com.todayit.common.exception.ErrorStatus;

/** 코스 기능에서 사용하는 오류 코드입니다. */
public enum CourseErrorCode implements ErrorCode {
  /** 코스명이 없거나 허용 길이를 벗어난 경우입니다. */
  INVALID_TITLE("INVALID_COURSE_TITLE", "코스명은 1자 이상 100자 이하이어야 합니다."),

  /** 지역 코드가 없거나 형식이 올바르지 않은 경우입니다. */
  INVALID_REGION("INVALID_COURSE_REGION", "시도 코드는 2자리, 시군구 코드는 3자리이어야 합니다."),

  /** 콘셉트 개수나 값, 중복 여부가 올바르지 않은 경우입니다. */
  INVALID_THEMES("INVALID_COURSE_THEMES", "콘셉트는 중복 없이 1개 이상 2개 이하로 입력해야 합니다."),

  /** 등록되지 않은 콘셉트를 요청한 경우입니다. */
  UNSUPPORTED_THEME("UNSUPPORTED_COURSE_THEME", "지원하지 않는 콘셉트가 포함되어 있습니다."),

  /** 시작 시각을 입력하지 않은 경우입니다. */
  START_AT_REQUIRED("COURSE_START_AT_REQUIRED", "시작 시각은 필수입니다."),

  /** 종료 시각이 시작 시각보다 늦지 않은 경우입니다. */
  INVALID_END_AT("INVALID_COURSE_END_AT", "종료 시각은 시작 시각 이후이어야 합니다."),

  /** 이동수단을 입력하지 않은 경우입니다. */
  TRANSPORT_REQUIRED("COURSE_TRANSPORT_REQUIRED", "이동수단은 필수입니다."),

  /** 요청한 코스가 존재하지 않거나 비공개 코스에 접근할 권한이 없는 경우입니다. */
  NOT_FOUND("COURSE_NOT_FOUND", "코스를 찾을 수 없습니다.", ErrorStatus.NOT_FOUND),

  /** 코스에 추가하려는 장소를 찾을 수 없거나 삭제된 경우입니다. */
  PLACE_NOT_FOUND("PLACE_NOT_FOUND", "장소를 찾을 수 없습니다.", ErrorStatus.NOT_FOUND),

  /** 코스 내 장소를 찾을 수 없는 경우입니다. */
  COURSE_PLACE_NOT_FOUND("COURSE_PLACE_NOT_FOUND", "코스 내 장소를 찾을 수 없습니다.", ErrorStatus.NOT_FOUND),

  /** 장소 식별자가 누락되었거나 유효하지 않은 경우입니다. */
  INVALID_PLACE_ID("INVALID_PLACE_ID", "장소 식별자는 1 이상이어야 합니다."),

  /** 방문 순서가 유효하지 않은 경우입니다. */
  INVALID_ORDER("INVALID_COURSE_PLACE_ORDER", "방문 순서는 1 이상이어야 합니다."),

  /** 체류 시간이 유효 범위를 벗어난 경우입니다. */
  INVALID_STAY_MINUTE("INVALID_STAY_MINUTE", "체류 시간은 0분 이상 1440분 이하이어야 합니다."),

  /** 동일한 장소를 허용 횟수(최대 2회)를 초과하여 추가하려는 경우입니다. */
  MAX_PLACE_DUPLICATION_EXCEEDED(
      "MAX_PLACE_DUPLICATION_EXCEEDED", "동일한 장소는 코스에 최대 2회까지만 추가할 수 있습니다."),

  /** 임시저장(DRAFT) 상태의 코스는 복사할 수 없는 경우입니다. */
  CANNOT_COPY_DRAFT("CANNOT_COPY_DRAFT", "임시저장 코스는 복사할 수 없습니다.");

  private final String code;
  private final String message;
  private final ErrorStatus status;

  CourseErrorCode(String code, String message) {
    this(code, message, ErrorStatus.BAD_REQUEST);
  }

  CourseErrorCode(String code, String message, ErrorStatus status) {
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

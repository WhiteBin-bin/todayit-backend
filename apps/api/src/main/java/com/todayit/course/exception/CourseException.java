package com.todayit.course.exception;

import com.todayit.common.exception.BusinessException;

/** 코스 생성 조건이 올바르지 않을 때 발생하는 예외입니다. */
public class CourseException extends BusinessException {

  /**
   * 코스 오류 코드에 해당하는 예외를 생성합니다.
   *
   * @param errorCode 코스 오류 코드
   */
  public CourseException(CourseErrorCode errorCode) {
    super(errorCode);
  }
}

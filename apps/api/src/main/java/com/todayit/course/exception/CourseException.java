package com.todayit.course.exception;

import com.todayit.common.exception.BusinessException;

/** 코스 기능의 업무 규칙 위반을 오류 코드로 구분하는 예외입니다. */
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

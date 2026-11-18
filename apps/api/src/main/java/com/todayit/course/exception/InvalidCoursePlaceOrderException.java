package com.todayit.course.exception;

/** 코스 장소 방문 순서가 유효하지 않을 때 발생하는 예외입니다. */
public class InvalidCoursePlaceOrderException extends CourseException {

  /** 기본 방문 순서 오류로 예외를 생성합니다. */
  public InvalidCoursePlaceOrderException() {
    super(CourseErrorCode.INVALID_ORDER);
  }
}

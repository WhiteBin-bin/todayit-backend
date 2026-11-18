package com.todayit.course.exception;

/** 코스를 찾을 수 없거나 접근 권한이 없을 때 발생하는 예외입니다. */
public class CourseNotFoundException extends CourseException {

  /** 코스 없음 오류를 생성합니다. */
  public CourseNotFoundException() {
    super(CourseErrorCode.NOT_FOUND);
  }
}

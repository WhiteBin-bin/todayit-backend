package com.todayit.course.exception;

/** 코스 내 장소를 찾을 수 없을 때 발생하는 예외입니다. */
public class CoursePlaceNotFoundException extends CourseException {

  /** 코스 장소 없음 오류를 생성합니다. */
  public CoursePlaceNotFoundException() {
    super(CourseErrorCode.COURSE_PLACE_NOT_FOUND);
  }
}

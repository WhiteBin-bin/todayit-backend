package com.todayit.course.exception;

/** 동일한 장소를 코스에 허용 횟수(최대 2회)를 초과하여 추가하려고 할 때 발생하는 예외입니다. */
public class MaxPlaceDuplicationExceededException extends CourseException {

  /** 기본 중복 초과 오류로 예외를 생성합니다. */
  public MaxPlaceDuplicationExceededException() {
    super(CourseErrorCode.MAX_PLACE_DUPLICATION_EXCEEDED);
  }
}

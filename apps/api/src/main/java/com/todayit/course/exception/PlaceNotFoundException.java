package com.todayit.course.exception;

/** 코스에 추가하려는 장소를 찾을 수 없거나 비활성화 또는 삭제된 경우 발생하는 예외입니다. */
public class PlaceNotFoundException extends CourseException {

  /** 기본 장소 미존재 오류로 예외를 생성합니다. */
  public PlaceNotFoundException() {
    super(CourseErrorCode.PLACE_NOT_FOUND);
  }
}

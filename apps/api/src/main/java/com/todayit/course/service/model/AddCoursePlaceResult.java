package com.todayit.course.service.model;

import com.todayit.course.entity.CoursePlace;

/**
 * 코스 장소 추가 결과 모델입니다.
 *
 * @param coursePlaceId 코스 장소 식별자
 * @param placeId 장소 식별자
 * @param order 방문 순서
 * @param stayMinute 체류 시간(분)
 */
public record AddCoursePlaceResult(int coursePlaceId, int placeId, int order, int stayMinute) {

  /**
   * CoursePlace 엔티티로부터 결과 모델을 생성합니다.
   *
   * @param coursePlace 코스 장소 엔티티
   * @return 코스 장소 추가 결과 모델
   */
  public static AddCoursePlaceResult from(CoursePlace coursePlace) {
    return new AddCoursePlaceResult(
        coursePlace.getCoursePlaceId(),
        coursePlace.getPlaceId(),
        coursePlace.getOrder(),
        coursePlace.getStayMinute());
  }
}

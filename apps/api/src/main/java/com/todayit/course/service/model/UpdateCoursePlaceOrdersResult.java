package com.todayit.course.service.model;

import java.util.List;

/**
 * 코스 장소 순서 변경 결과 모델입니다.
 *
 * @param places 변경된 전체 코스 장소 목록
 */
public record UpdateCoursePlaceOrdersResult(List<CoursePlaceDetailResult> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param places 전체 코스 장소 목록
   */
  public UpdateCoursePlaceOrdersResult {
    places = places == null ? List.of() : List.copyOf(places);
  }
}

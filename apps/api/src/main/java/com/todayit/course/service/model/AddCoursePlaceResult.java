package com.todayit.course.service.model;

import java.util.List;

/**
 * 코스 장소 추가 결과 모델입니다.
 *
 * @param addedPlace 추가된 코스 장소 상세 정보
 * @param places 장소 추가 후 최신 정렬된 전체 코스 장소 목록
 */
public record AddCoursePlaceResult(
    CoursePlaceDetailResult addedPlace, List<CoursePlaceDetailResult> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param addedPlace 추가된 코스 장소 상세 정보
   * @param places 전체 코스 장소 목록
   */
  public AddCoursePlaceResult {
    places = places == null ? List.of() : List.copyOf(places);
  }
}

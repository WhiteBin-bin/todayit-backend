package com.todayit.course.service.model;

import java.util.List;

/**
 * 코스 장소 삭제 결과 모델입니다.
 *
 * @param deletedCoursePlaceId 삭제된 코스 장소 식별자
 * @param places 삭제 후 재정렬된 남은 코스 장소 목록
 */
public record RemoveCoursePlaceResult(
    int deletedCoursePlaceId, List<CoursePlaceDetailResult> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param deletedCoursePlaceId 삭제된 코스 장소 식별자
   * @param places 남은 장소 목록
   */
  public RemoveCoursePlaceResult {
    places = places == null ? List.of() : List.copyOf(places);
  }
}

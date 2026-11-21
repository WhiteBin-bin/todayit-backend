package com.todayit.course.service.model;

import com.todayit.course.entity.CoursePlace;
import java.util.List;

/**
 * 코스 장소 추가 후 엔티티 묶음 모델입니다.
 *
 * @param addedPlace 추가된 코스 장소 엔티티
 * @param places 전체 코스 장소 엔티티 목록
 */
public record AddCoursePlaceEntities(CoursePlace addedPlace, List<CoursePlace> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param addedPlace 추가된 장소 엔티티
   * @param places 전체 장소 엔티티 목록
   */
  public AddCoursePlaceEntities {
    places = places == null ? List.of() : List.copyOf(places);
  }
}

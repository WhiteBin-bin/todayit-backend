package com.todayit.course.service.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 생성 조건 수정 결과 모델입니다.
 *
 * @param courseId 코스 식별자
 * @param conditions 수정된 생성 조건
 * @param places 코스에 포함된 장소 목록
 * @param updatedAt 수정 일시
 * @param scrapCount 코스의 활성 스크랩 수
 */
public record UpdateCourseConditionsResult(
    int courseId,
    CourseConditionsResult conditions,
    List<CoursePlaceDetailResult> places,
    LocalDateTime updatedAt,
    long scrapCount) {

  /**
   * 장소 목록을 방어적으로 복사하여 초기화합니다.
   *
   * @param courseId 코스 식별자
   * @param conditions 수정된 생성 조건
   * @param places 코스에 포함된 장소 목록
   * @param updatedAt 수정 일시
   * @param scrapCount 코스의 활성 스크랩 수
   */
  public UpdateCourseConditionsResult {
    places = places != null ? List.copyOf(places) : List.of();
  }
}

package com.todayit.course.service.model;

import com.todayit.course.entity.CourseStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 복사 결과 모델입니다.
 *
 * @param courseId 새로 생성된 코스 식별자
 * @param originalCourseId 원본 코스 식별자
 * @param title 코스명 (복사 직후 null)
 * @param status 코스 상태 (DRAFT)
 * @param places 복제된 코스 장소 목록
 * @param createdAt 생성 일시
 */
public record CopyCourseResult(
    int courseId,
    int originalCourseId,
    String title,
    CourseStatus status,
    List<CoursePlaceDetailResult> places,
    LocalDateTime createdAt) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param courseId 새로 생성된 코스 식별자
   * @param originalCourseId 원본 코스 식별자
   * @param title 코스명
   * @param status 코스 상태
   * @param places 복제된 장소 목록
   * @param createdAt 생성 일시
   */
  public CopyCourseResult {
    places = places == null ? List.of() : List.copyOf(places);
  }
}

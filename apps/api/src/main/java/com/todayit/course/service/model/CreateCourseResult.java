package com.todayit.course.service.model;

import com.todayit.course.entity.Course;

/**
 * 코스 생성 결과입니다.
 *
 * @param courseId 코스 식별자
 */
public record CreateCourseResult(int courseId) {

  /**
   * 코스 Entity의 생성 정보를 서비스 결과로 변환합니다.
   *
   * @param course 저장한 코스
   * @return 코스 생성 결과
   */
  public static CreateCourseResult from(Course course) {
    return new CreateCourseResult(course.getSnapshot().courseId());
  }
}

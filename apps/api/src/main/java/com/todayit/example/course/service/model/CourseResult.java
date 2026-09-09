package com.todayit.example.course.service.model;

import com.todayit.example.course.entity.Course;

/**
 * HTTP에 의존하지 않는 생성 결과입니다.
 *
 * @param id 코스 식별자
 * @param name 코스 이름
 */
public record CourseResult(String id, String name) {
  /**
   * Entity에서 결과에 필요한 값을 꺼냅니다.
   *
   * @param course 저장된 코스
   * @return 생성 결과
   */
  public static CourseResult from(Course course) {
    return new CourseResult(course.getId(), course.getName());
  }
}

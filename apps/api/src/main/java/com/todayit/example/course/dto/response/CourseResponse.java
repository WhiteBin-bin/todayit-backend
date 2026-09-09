package com.todayit.example.course.dto.response;

import com.todayit.example.course.service.model.CourseResult;

/**
 * Entity 대신 외부로 전달할 응답입니다.
 *
 * @param id 코스 식별자
 * @param name 코스 이름
 */
public record CourseResponse(String id, String name) {
  /**
   * 업무 결과에서 공개할 값을 선택합니다.
   *
   * @param result 생성 결과
   * @return 코스 응답
   */
  public static CourseResponse from(CourseResult result) {
    return new CourseResponse(result.id(), result.name());
  }
}

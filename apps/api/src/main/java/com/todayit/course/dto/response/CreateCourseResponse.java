package com.todayit.course.dto.response;

import com.todayit.course.service.model.CreateCourseResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 생성 응답입니다.
 *
 * @param courseId 생성된 코스 식별자
 */
@Schema(description = "새로 생성한 임시 코스 식별자 정보입니다.")
public record CreateCourseResponse(
    @Schema(
            description = "생성된 코스의 식별자입니다.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId) {

  /**
   * 생성 결과를 HTTP 응답으로 변환합니다.
   *
   * @param result 생성 결과
   * @return 코스 생성 응답
   */
  public static CreateCourseResponse from(CreateCourseResult result) {
    return new CreateCourseResponse(result.courseId());
  }
}

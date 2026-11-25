package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.model.UpdateCourseTransportResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 이동수단 변경 응답 DTO입니다.
 *
 * @param courseId 코스 식별자
 * @param transport 변경된 이동수단
 */
@Schema(description = "코스 이동수단 변경 응답")
public record UpdateCourseTransportResponse(
    @Schema(description = "코스 식별자", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(description = "코스 이동수단", example = "CAR", requiredMode = Schema.RequiredMode.REQUIRED)
        CourseTransport transport) {

  /**
   * 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 이동수단 변경 결과 모델
   * @return 코스 이동수단 변경 응답 DTO
   */
  public static UpdateCourseTransportResponse from(UpdateCourseTransportResult result) {
    return new UpdateCourseTransportResponse(result.courseId(), result.transport());
  }
}

package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.model.UpdateCourseVisibilityResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 코스 공개 범위 설정 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param visibility 코스 공개 범위
 * @param updatedAt 수정 일시
 */
@Schema(description = "코스 공개 범위 설정 응답")
public record UpdateCourseVisibilityResponse(
    @Schema(description = "코스 식별자", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "코스 공개 범위",
            example = "PUBLIC",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseVisibility visibility,
    @Schema(
            description = "수정 일시",
            example = "2026-11-24T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt) {

  /**
   * 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 공개 범위 설정 결과
   * @return 코스 공개 범위 설정 응답
   */
  public static UpdateCourseVisibilityResponse from(UpdateCourseVisibilityResult result) {
    return new UpdateCourseVisibilityResponse(
        result.courseId(), result.visibility(), result.updatedAt());
  }
}

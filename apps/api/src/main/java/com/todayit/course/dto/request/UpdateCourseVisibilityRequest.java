package com.todayit.course.dto.request;

import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.CourseVisibility;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 공개 범위 설정 요청입니다.
 *
 * @param visibility 코스 공개 범위
 */
@Schema(description = "코스 공개 범위 설정 요청")
public record UpdateCourseVisibilityRequest(
    @Schema(
            description =
                """
                변경할 코스 공개 범위입니다.

                - `PUBLIC`: 공개
                - `PRIVATE`: 비공개
                """,
            example = "PUBLIC",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseVisibility visibility) {

  /**
   * 입력값을 검증합니다.
   *
   * @throws InvalidRequestException 공개 범위가 누락된 경우
   */
  public void validate() {
    if (visibility == null) {
      throw new InvalidRequestException("공개 범위는 필수입니다.");
    }
  }
}

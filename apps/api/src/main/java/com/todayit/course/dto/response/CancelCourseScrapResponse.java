package com.todayit.course.dto.response;

import com.todayit.course.service.model.CourseScrapResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 스크랩 취소 API 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param scrapped 현재 회원의 스크랩 여부
 */
@Schema(description = "코스 스크랩 취소 응답")
public record CancelCourseScrapResponse(
    @Schema(description = "코스 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "현재 회원이 이 코스를 스크랩한 상태인지 나타냅니다.",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean scrapped) {

  /**
   * 코스 스크랩 취소 결과를 API 응답으로 변환합니다.
   *
   * @param result 코스 스크랩 취소 결과
   * @return 코스 스크랩 취소 응답
   */
  public static CancelCourseScrapResponse from(CourseScrapResult result) {
    return new CancelCourseScrapResponse(result.courseId(), result.scrapped());
  }
}

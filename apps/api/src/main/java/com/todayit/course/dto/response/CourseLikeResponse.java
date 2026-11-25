package com.todayit.course.dto.response;

import com.todayit.course.service.model.CourseLikeResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 좋아요 API 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param liked 현재 회원의 좋아요 여부
 * @param likeCount 코스의 좋아요 수
 */
@Schema(description = "코스 좋아요 응답")
public record CourseLikeResponse(
    @Schema(description = "코스 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "현재 회원이 이 코스에 좋아요를 눌렀는지 나타냅니다.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean liked,
    @Schema(
            description = "코스의 전체 좋아요 수입니다.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
        long likeCount) {

  /**
   * 코스 좋아요 결과를 API 응답으로 변환합니다.
   *
   * @param result 코스 좋아요 결과
   * @return 코스 좋아요 응답
   */
  public static CourseLikeResponse from(CourseLikeResult result) {
    return new CourseLikeResponse(result.courseId(), result.liked(), result.likeCount());
  }
}

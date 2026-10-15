package com.todayit.place.dto.response;

import com.todayit.place.service.model.PlaceLikeResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 장소 좋아요 API 응답입니다.
 *
 * @param placeId 장소 식별자
 * @param liked 현재 회원의 좋아요 여부
 * @param likeCount 장소의 좋아요 수
 */
@Schema(description = "장소 좋아요 응답")
public record PlaceLikeResponse(
    @Schema(description = "장소 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int placeId,
    @Schema(
            description = "현재 회원이 이 장소에 좋아요를 눌렀는지 나타냅니다.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean liked,
    @Schema(
            description = "장소의 전체 좋아요 수입니다.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
        long likeCount) {

  /**
   * 장소 좋아요 결과를 API 응답으로 변환합니다.
   *
   * @param result 장소 좋아요 결과
   * @return 장소 좋아요 응답
   */
  public static PlaceLikeResponse from(PlaceLikeResult result) {
    return new PlaceLikeResponse(result.placeId(), result.liked(), result.likeCount());
  }
}

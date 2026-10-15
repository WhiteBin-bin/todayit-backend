package com.todayit.place.dto.response;

import com.todayit.place.service.model.PlaceScrapResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 장소 스크랩 API 응답입니다.
 *
 * @param placeId 장소 식별자
 * @param scrapped 현재 회원의 스크랩 여부
 * @param scrapCount 장소의 활성 스크랩 수
 */
@Schema(description = "장소 스크랩 응답")
public record PlaceScrapResponse(
    @Schema(description = "장소 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int placeId,
    @Schema(
            description = "현재 회원이 이 장소를 스크랩했는지 나타냅니다.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean scrapped,
    @Schema(
            description = "취소되지 않은 장소 스크랩 수입니다.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
        long scrapCount) {

  /**
   * 장소 스크랩 결과를 API 응답으로 변환합니다.
   *
   * @param result 장소 스크랩 결과
   * @return 장소 스크랩 응답
   */
  public static PlaceScrapResponse from(PlaceScrapResult result) {
    return new PlaceScrapResponse(result.placeId(), result.scrapped(), result.scrapCount());
  }
}

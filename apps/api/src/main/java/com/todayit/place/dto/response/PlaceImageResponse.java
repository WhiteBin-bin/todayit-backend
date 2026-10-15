package com.todayit.place.dto.response;

import com.todayit.place.service.model.PlaceImageResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 장소 사진 응답입니다.
 *
 * @param placeImageId 장소 사진 식별자
 * @param imageUrl 장소 사진 URL
 */
@Schema(description = "장소 사진 응답")
public record PlaceImageResponse(
    @Schema(
            description = "장소 사진 식별자입니다.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int placeImageId,
    @Schema(
            description = "장소 사진 URL입니다.",
            example = "https://example.com/images/place-1.jpg",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String imageUrl) {

  /**
   * 장소 사진 결과를 API 응답으로 변환합니다.
   *
   * @param result 장소 사진 결과
   * @return 장소 사진 응답
   */
  public static PlaceImageResponse from(PlaceImageResult result) {
    return new PlaceImageResponse(result.placeImageId(), result.imageUrl());
  }
}

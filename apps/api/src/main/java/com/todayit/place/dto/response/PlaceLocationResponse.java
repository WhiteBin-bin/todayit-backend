package com.todayit.place.dto.response;

import com.todayit.place.service.model.PlaceLocationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * 장소 지도 조회 API 응답입니다.
 *
 * @param placeId 장소 식별자
 * @param latitude 위도
 * @param longitude 경도
 * @param address 주소
 */
@Schema(description = "장소 지도 조회 응답")
public record PlaceLocationResponse(
    @Schema(description = "장소 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int placeId,
    @Schema(
            description = "장소의 위도입니다.",
            example = "37.5665",
            requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal latitude,
    @Schema(
            description = "장소의 경도입니다.",
            example = "126.9780",
            requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal longitude,
    @Schema(
            description = "장소 주소입니다.",
            example = "서울특별시 중구 세종대로 110",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String address) {

  /**
   * 장소 위치 조회 결과를 API 응답으로 변환합니다.
   *
   * @param result 장소 위치 조회 결과
   * @return 장소 위치 응답
   */
  public static PlaceLocationResponse from(PlaceLocationResult result) {
    return new PlaceLocationResponse(
        result.placeId(), result.latitude(), result.longitude(), result.address());
  }
}

package com.todayit.place.dto.response;

import com.todayit.place.service.model.PlaceShareResult;
import java.time.LocalDateTime;

/**
 * 장소 공유 API 응답입니다.
 *
 * @param placeId 장소 식별자
 * @param shareUrl 공유 링크 URL
 * @param expiresAt 공유 만료 일시, 만료 기간이 설정되지 않은 경우 {@code null}
 */
public record PlaceShareResponse(int placeId, String shareUrl, LocalDateTime expiresAt) {

  /**
   * 장소 공유 결과를 API 응답으로 변환합니다.
   *
   * @param result 장소 공유 결과
   * @return 장소 공유 응답
   */
  public static PlaceShareResponse from(PlaceShareResult result) {
    return new PlaceShareResponse(result.placeId(), result.shareUrl(), result.expiresAt());
  }
}

package com.todayit.place.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 장소 공유 링크 생성에 필요한 설정값입니다.
 *
 * @param baseUrl 공유 링크의 기본 URL
 * @param expiration 공유 링크 만료 기간
 */
@ConfigurationProperties(prefix = "todayit.place.share")
public record PlaceShareProperties(String baseUrl, Duration expiration) {

  /**
   * 장소 식별자를 기반으로 공유 URL을 생성합니다.
   *
   * @param placeId 장소 식별자
   * @return 생성된 공유 URL
   */
  public String createShareUrl(int placeId) {
    if (baseUrl.contains("{placeId}")) {
      return baseUrl.replace("{placeId}", String.valueOf(placeId));
    }
    return baseUrl.replaceAll("/+$", "") + "/" + placeId;
  }
}

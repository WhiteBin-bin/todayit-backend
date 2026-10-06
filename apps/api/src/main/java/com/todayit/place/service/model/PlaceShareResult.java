package com.todayit.place.service.model;

import java.time.LocalDateTime;

/**
 * 장소 공유 처리 결과입니다.
 *
 * @param placeId 장소 식별자
 * @param shareUrl 공유 링크 URL
 * @param expiresAt 공유 만료 일시, 만료 기간이 설정되지 않은 경우 {@code null}
 */
public record PlaceShareResult(int placeId, String shareUrl, LocalDateTime expiresAt) {}

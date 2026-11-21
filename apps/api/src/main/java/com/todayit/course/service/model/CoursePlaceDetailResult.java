package com.todayit.course.service.model;

import java.math.BigDecimal;

/**
 * 코스에 포함된 장소 상세 정보입니다.
 *
 * @param coursePlaceId 코스 장소 식별자
 * @param placeId 장소 식별자
 * @param name 장소명
 * @param category 카테고리
 * @param address 주소
 * @param latitude 위도
 * @param longitude 경도
 * @param imageUrl 대표 이미지 URL, 없으면 null
 * @param order 방문 순서
 * @param stayMinute 머무는 시간(분), 미입력 시 null
 */
public record CoursePlaceDetailResult(
    int coursePlaceId,
    int placeId,
    String name,
    String category,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    String imageUrl,
    int order,
    Integer stayMinute) {}

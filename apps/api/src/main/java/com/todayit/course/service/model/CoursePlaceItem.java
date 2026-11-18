package com.todayit.course.service.model;

import java.math.BigDecimal;

/**
 * 코스에서 참조하는 장소 정보입니다.
 *
 * @param placeId 장소 식별자
 * @param name 장소명
 * @param category 카테고리
 * @param address 주소
 * @param latitude 위도
 * @param longitude 경도
 * @param imageUrl 대표 이미지 URL, 없으면 null
 */
public record CoursePlaceItem(
    int placeId,
    String name,
    String category,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    String imageUrl) {}

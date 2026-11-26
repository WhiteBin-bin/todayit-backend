package com.todayit.course.service.model;

/**
 * 장소의 시·도 및 시·군·구 코드입니다.
 *
 * @param placeId 장소 식별자
 * @param sidoCode 시·도 코드
 * @param sigunguCode 시·군·구 코드
 */
public record CoursePlaceRegion(int placeId, String sidoCode, String sigunguCode) {}

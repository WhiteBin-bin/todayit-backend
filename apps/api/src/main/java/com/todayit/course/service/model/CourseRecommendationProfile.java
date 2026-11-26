package com.todayit.course.service.model;

/**
 * 추천에 사용할 회원 선호 정보입니다.
 *
 * @param personalizedConsent 선호정보 이용 동의 여부
 * @param sidoCode 선호 시·도 코드, 미설정 시 null
 * @param sigunguCode 선호 시·군·구 코드, 미설정 시 null
 */
public record CourseRecommendationProfile(
    boolean personalizedConsent, String sidoCode, String sigunguCode) {}

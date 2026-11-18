package com.todayit.course.service.model;

/**
 * 코스 지역 요약 정보입니다.
 *
 * @param sidoCode 시·도 코드
 * @param sidoName 시·도 이름
 * @param sigunguCode 시·군·구 코드, 시·도 전체 대상인 경우 null
 * @param sigunguName 시·군·구 이름, 시·도 전체 대상인 경우 null
 */
public record CourseRegionResult(
    String sidoCode, String sidoName, String sigunguCode, String sigunguName) {}

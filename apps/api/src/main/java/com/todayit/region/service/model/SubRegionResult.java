package com.todayit.region.service.model;

/**
 * 세부 지역 조회 결과 모델입니다.
 *
 * @param sigunguCode 시·군·구 코드
 * @param sigunguName 세부 지역 명칭
 */
public record SubRegionResult(String sigunguCode, String sigunguName) {}

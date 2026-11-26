package com.todayit.region.service.model;

import java.util.List;

/**
 * 시·군·구 및 하위 세부 지역 목록을 포함하는 조회 결과 모델입니다.
 *
 * @param sigunguCode 시·군·구 코드
 * @param sigunguName 시·군·구 명칭
 * @param subRegions 하위 세부 지역 결과 목록
 */
public record SigunguResult(
    String sigunguCode, String sigunguName, List<SubRegionResult> subRegions) {

  /**
   * 세부 지역 목록을 방어적으로 복사하여 초기화합니다.
   *
   * @param sigunguCode 시·군·구 코드
   * @param sigunguName 시·군·구 명칭
   * @param subRegions 하위 세부 지역 결과 목록
   */
  public SigunguResult {
    subRegions = subRegions != null ? List.copyOf(subRegions) : List.of();
  }

  /**
   * 하위 세부 지역이 없는 시·군·구 결과 모델을 생성합니다.
   *
   * @param sigunguCode 시·군·구 코드
   * @param sigunguName 시·군·구 명칭
   */
  public SigunguResult(String sigunguCode, String sigunguName) {
    this(sigunguCode, sigunguName, List.of());
  }
}

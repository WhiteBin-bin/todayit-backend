package com.todayit.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 지역 필터의 공통 입력 및 검증 결과입니다.
 *
 * @param sidoCode 시·도 코드 2자리
 * @param sigunguCodes 시·군·구 코드 목록, 비어 있으면 해당 시·도 전체
 */
public record RegionFilterValue(String sidoCode, List<String> sigunguCodes) {
  /** 목록을 방어적으로 복사하고 생략된 시·군·구 목록을 빈 목록으로 처리합니다. */
  public RegionFilterValue {
    sigunguCodes =
        sigunguCodes == null
            ? List.of()
            : Collections.unmodifiableList(new ArrayList<>(sigunguCodes));
  }
}

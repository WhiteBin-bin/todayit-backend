package com.todayit.course.service;

import com.todayit.course.exception.InvalidRegionPathException;
import java.util.List;

/** 장소 기능에서 지역 경로를 검증하고 조회하기 위한 서비스 계약입니다. */
public interface RegionPlaceQueryService {

  /**
   * 단일 지역명 또는 상위 지역부터 전달된 지역 경로를 검증하고 해당하는 지역 식별자를 반환합니다.
   *
   * @param regionPath 단일 시·도·시·군·구 이름 또는 공백으로 구분한 지역 경로
   * @return 지역 경로에 포함되는 지역 식별자 목록
   * @throws InvalidRegionPathException 지역 경로가 비어 있거나 존재하지 않을 때
   */
  List<Integer> findRegionIds(String regionPath);
}

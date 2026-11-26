package com.todayit.course.service;

import com.todayit.course.service.model.CoursePlaceRegion;
import java.util.List;

/** 추천 코스의 장소별 행정구역 조회 계약입니다. */
public interface PlaceCourseRegionQueryService {
  /**
   * 장소별 확인된 행정구역을 조회합니다.
   *
   * @param placeIds 장소 식별자 목록
   * @return 확인된 장소와 지역 코드 목록
   */
  List<CoursePlaceRegion> findPlaceRegions(List<Integer> placeIds);
}

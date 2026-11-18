package com.todayit.course.service;

import com.todayit.course.service.model.CoursePlaceItem;
import java.util.List;
import java.util.Map;

/** 코스 기능에서 장소 정보를 조회하기 위한 서비스 계약입니다. */
public interface PlaceCourseQueryService {

  /**
   * 장소 식별자 목록에 해당하는 활성 장소 정보 맵을 조회합니다.
   *
   * @param placeIds 장소 식별자 목록
   * @return 장소 식별자별 코스용 장소 정보 맵
   */
  Map<Integer, CoursePlaceItem> findPlacesByIds(List<Integer> placeIds);

  /**
   * 장소가 활성 상태이고 삭제되지 않았는지 확인합니다.
   *
   * @param placeId 장소 식별자
   * @return 활성 장소 존재 여부
   */
  boolean existsActivePlace(int placeId);
}

package com.todayit.place.service;

import com.todayit.course.service.PlaceCourseRegionQueryService;
import com.todayit.course.service.model.CoursePlaceRegion;
import com.todayit.place.repository.PlaceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 코스 추천에 필요한 장소 지역을 조회합니다. */
@Service
public class PlaceRecommendationQueryService implements PlaceCourseRegionQueryService {
  private final PlaceRepository places;

  /**
   * 장소 저장소를 받습니다.
   *
   * @param places 장소 저장소
   */
  public PlaceRecommendationQueryService(PlaceRepository places) {
    this.places = places;
  }

  /**
   * 장소 식별자에 해당하는 행정구역 코드를 조회합니다.
   *
   * @param placeIds 장소 식별자 목록
   * @return 확인된 장소와 지역 코드 목록
   */
  @Override
  @Transactional(readOnly = true)
  public List<CoursePlaceRegion> findPlaceRegions(List<Integer> placeIds) {
    return placeIds.isEmpty() ? List.of() : places.findCoursePlaceRegions(placeIds);
  }
}

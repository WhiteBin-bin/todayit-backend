package com.todayit.place.service;

import com.todayit.course.service.CoursePlaceQueryService;
import com.todayit.place.service.model.PlaceDetailResult;
import com.todayit.place.service.model.PlaceDetailWithStayResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 장소 상세와 코스의 평균 체류 시간을 조합합니다. */
@Service
public class PlaceDetailFacade {
  private final PlaceDetailQueryService places;
  private final CoursePlaceQueryService courses;

  /**
   * 장소와 코스 조회 서비스를 주입합니다.
   *
   * @param places 장소 조회
   * @param courses 코스 조회
   */
  public PlaceDetailFacade(PlaceDetailQueryService places, CoursePlaceQueryService courses) {
    this.places = places;
    this.courses = courses;
  }

  /**
   * 장소가 조회 가능한 상태인지 확인한 후 평균 체류 시간을 조합합니다.
   *
   * @param placeId 장소 식별자
   * @param memberId 회원 식별자, 비회원이면 null
   * @return 조회 결과
   */
  @Transactional(readOnly = true)
  public PlaceDetailWithStayResult getPlaceDetail(int placeId, String memberId) {
    PlaceDetailResult detail = places.getPlaceDetail(placeId, memberId);
    return new PlaceDetailWithStayResult(detail, courses.getAverageStayMinutes(placeId));
  }
}

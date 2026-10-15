package com.todayit.place.repository;

import com.todayit.place.entity.Category;
import com.todayit.place.entity.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 장소 정보를 조회하고 저장하는 JPA Repository입니다. */
public interface PlaceRepository extends JpaRepository<Place, Integer> {

  /**
   * 활성화되고 삭제되지 않은 장소를 페이지 단위로 조회합니다.
   *
   * @param regionIds 선택한 지역 경로에 포함되는 지역 식별자 목록
   * @param regionFiltered 지역 필터 적용 여부
   * @param categories 장소 카테고리 목록
   * @param categoryFiltered 카테고리 필터 적용 여부
   * @param pageable 페이지와 정렬 조건
   * @return 조건에 맞는 장소 페이지
   */
  @Query(
      value =
          "select distinct place from Place place "
              + "where place.isActive = true and place.isDeleted = false "
              + "and (:regionFiltered = false or exists ("
              + "select regionPlace.regionPlaceId from RegionPlace regionPlace "
              + "where regionPlace.place = place "
              + "and regionPlace.region.regionId in :regionIds)) "
              + "and (:categoryFiltered = false or place.category in :categories)",
      countQuery =
          "select count(distinct place) from Place place "
              + "where place.isActive = true and place.isDeleted = false "
              + "and (:regionFiltered = false or exists ("
              + "select regionPlace.regionPlaceId from RegionPlace regionPlace "
              + "where regionPlace.place = place "
              + "and regionPlace.region.regionId in :regionIds)) "
              + "and (:categoryFiltered = false or place.category in :categories)")
  Page<Place> findPlaces(
      @Param("regionIds") List<Integer> regionIds,
      @Param("regionFiltered") boolean regionFiltered,
      @Param("categories") List<Category> categories,
      @Param("categoryFiltered") boolean categoryFiltered,
      Pageable pageable);

  /**
   * 활성화되고 삭제되지 않은 장소를 식별자로 조회합니다.
   *
   * @param placeId 장소 식별자
   * @return 조건에 맞는 장소
   */
  Optional<Place> findByPlaceIdAndIsActiveTrueAndIsDeletedFalse(int placeId);
}

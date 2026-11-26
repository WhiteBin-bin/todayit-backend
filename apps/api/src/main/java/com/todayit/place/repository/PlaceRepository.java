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
   * 코스의 장소별 행정구역을 일괄 조회합니다.
   *
   * @param placeIds 장소 식별자 목록
   * @return 확인된 장소 지역 목록
   */
  @Query(
      "select new com.todayit.course.service.model.CoursePlaceRegion("
          + "rp.place.placeId, rp.region.sidoCode, rp.region.sigunguCode) "
          + "from RegionPlace rp where rp.place.placeId in :placeIds")
  List<com.todayit.course.service.model.CoursePlaceRegion> findCoursePlaceRegions(
      @Param("placeIds") List<Integer> placeIds);

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

  /**
   * 활성화되고 삭제되지 않은 장소가 존재하는지 확인합니다.
   *
   * @param placeId 장소 식별자
   * @return 활성 장소 존재 여부
   */
  boolean existsByPlaceIdAndIsActiveTrueAndIsDeletedFalse(int placeId);

  /**
   * 활성화되고 삭제되지 않은 장소들을 식별자 목록으로 조회합니다.
   *
   * @param placeIds 장소 식별자 목록
   * @return 조건에 맞는 장소 목록
   */
  List<Place> findByPlaceIdInAndIsActiveTrueAndIsDeletedFalse(List<Integer> placeIds);

  /**
   * 이름에 검색어 글자가 하나 이상 포함된 활성 장소를 조회합니다. 공백을 제거한 이름의 완전 일치를 우선하고, 글자별 등장 횟수 중 작은 값을 합산한 점수 내림차순으로
   * 정렬합니다.
   *
   * @param keyword 가게 이름 검색어
   * @param pageable 페이지 조건
   * @return 검색 조건에 맞는 장소 페이지
   */
  @Query(
      value =
          """
          with search as (
            select regexp_replace(lower(:keyword), '[[:space:]]+', '', 'g') as keyword
          ), characters as (
            select ch, count(*) as occurrences
            from search, lateral regexp_split_to_table(keyword, '') ch
            where ch <> '' group by ch
          )
          select p.* from place p
          cross join search s
          cross join lateral (\n            select regexp_replace(lower(p.name), '[[:space:]]+', '', 'g') as name
          ) normalized
          cross join lateral (\n            select coalesce(sum(least(c.occurrences,
              char_length(normalized.name) - char_length(replace(normalized.name, c.ch, '')))), 0) as score
            from characters c
          ) matched
          where p.is_active = true and p.is_deleted = false and matched.score > 0
          order by (normalized.name = s.keyword) desc, matched.score desc,
            p.created_at desc, p.place_id desc
          """,
      countQuery =
          """
          select count(*) from place p where p.is_active = true and p.is_deleted = false
          and exists (
            select 1 from regexp_split_to_table(
              regexp_replace(lower(:keyword), '[[:space:]]+', '', 'g'), '') ch
            where ch <> '' and position(ch in lower(p.name)) > 0
          )
          """,
      nativeQuery = true)
  Page<Place> searchByName(@Param("keyword") String keyword, Pageable pageable);
}

package com.todayit.course.repository;

import com.todayit.course.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 행정 지역 정보를 조회하는 JPA Repository입니다. */
public interface RegionRepository extends JpaRepository<Region, Integer> {

  /**
   * 시·도에 속한 모든 지역 식별자를 조회합니다.
   *
   * @param si 시·도
   * @return 시·도에 속한 지역 식별자 목록
   */
  @Query("select region.regionId from Region region where region.si = :si")
  List<Integer> findIdsBySi(@Param("si") String si);

  /**
   * 시·군 이름이 일치하는 모든 지역 식별자를 조회합니다.
   *
   * @param gun 시·군
   * @return 시·군과 그 하위 구에 속한 지역 식별자 목록
   */
  @Query("select region.regionId from Region region where region.gun = :gun")
  List<Integer> findIdsByGun(@Param("gun") String gun);

  /**
   * 구 이름이 일치하는 모든 지역 식별자를 조회합니다.
   *
   * @param gu 구
   * @return 동일한 구 이름을 가진 지역 식별자 목록
   */
  @Query("select region.regionId from Region region where region.gu = :gu")
  List<Integer> findIdsByGu(@Param("gu") String gu);

  /**
   * 시·도 안에서 시·군 또는 구가 일치하는 지역 식별자를 조회합니다.
   *
   * @param si 시·도
   * @param subdivision 시·군 또는 구
   * @return 상하위 경로가 일치하는 지역 식별자 목록
   */
  @Query(
      "select region.regionId from Region region "
          + "where region.si = :si "
          + "and (region.gun = :subdivision or region.gu = :subdivision)")
  List<Integer> findIdsBySiAndSubdivision(
      @Param("si") String si, @Param("subdivision") String subdivision);

  /**
   * 시·군과 구가 일치하는 지역 식별자를 조회합니다.
   *
   * @param gun 시·군
   * @param gu 구
   * @return 시·군과 구가 일치하는 지역 식별자 목록
   */
  @Query(
      "select region.regionId from Region region " + "where region.gun = :gun and region.gu = :gu")
  List<Integer> findIdsByGunAndGu(@Param("gun") String gun, @Param("gu") String gu);

  /**
   * 시·도, 시·군과 구가 모두 일치하는 지역 식별자를 조회합니다.
   *
   * @param si 시·도
   * @param gun 시·군
   * @param gu 구
   * @return 전체 지역 경로가 일치하는 지역 식별자 목록
   */
  @Query(
      "select region.regionId from Region region "
          + "where region.si = :si and region.gun = :gun and region.gu = :gu")
  List<Integer> findIdsBySiAndGunAndGu(
      @Param("si") String si, @Param("gun") String gun, @Param("gu") String gu);
}

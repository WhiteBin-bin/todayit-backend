package com.todayit.course.service;

import com.todayit.course.exception.InvalidRegionPathException;
import com.todayit.course.repository.RegionRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 행정 지역의 상하위 경로를 검증하고 조회하는 Query Service입니다. */
@Service
public class RegionQueryService implements RegionPlaceQueryService {

  private static final int MAX_REGION_DEPTH = 3;
  private final RegionRepository regionRepository;

  /**
   * 지역 조회에 필요한 Repository를 받습니다.
   *
   * @param regionRepository 지역 Repository
   */
  public RegionQueryService(RegionRepository regionRepository) {
    this.regionRepository = regionRepository;
  }

  /**
   * 단일 지역명 또는 상위 지역부터 전달된 지역 경로를 검증하고 해당하는 지역 식별자를 반환합니다.
   *
   * @param regionPath 단일 시·도·시·군·구 이름 또는 공백으로 구분한 지역 경로
   * @return 지역 경로에 포함되는 지역 식별자 목록
   * @throws InvalidRegionPathException 지역 경로가 비어 있거나 존재하지 않을 때
   */
  @Transactional(readOnly = true)
  @Override
  public List<Integer> findRegionIds(String regionPath) {
    if (regionPath == null || regionPath.isBlank()) {
      throw new InvalidRegionPathException();
    }

    List<String> regions =
        Arrays.stream(regionPath.trim().split("\\s+")).filter(region -> !region.isBlank()).toList();
    if (regions.isEmpty() || regions.size() > MAX_REGION_DEPTH) {
      throw new InvalidRegionPathException();
    }

    List<Integer> regionIds = findRegionIds(regions);
    if (regionIds.isEmpty()) {
      throw new InvalidRegionPathException();
    }
    return List.copyOf(regionIds);
  }

  private List<Integer> findRegionIds(List<String> regions) {
    return switch (regions.size()) {
      case 1 -> findRegionIdsBySingleName(regions.get(0));
      case 2 -> findRegionIdsByTwoLevelPath(regions.get(0), regions.get(1));
      case 3 ->
          regionRepository.findIdsBySiAndGunAndGu(regions.get(0), regions.get(1), regions.get(2));
      default -> List.of();
    };
  }

  private List<Integer> findRegionIdsBySingleName(String region) {
    List<Integer> regionIds = regionRepository.findIdsBySi(region);
    if (!regionIds.isEmpty()) {
      return regionIds;
    }

    regionIds = regionRepository.findIdsByGun(region);
    return regionIds.isEmpty() ? regionRepository.findIdsByGu(region) : regionIds;
  }

  private List<Integer> findRegionIdsByTwoLevelPath(String parentRegion, String childRegion) {
    List<Integer> regionIds = regionRepository.findIdsBySiAndSubdivision(parentRegion, childRegion);
    return regionIds.isEmpty()
        ? regionRepository.findIdsByGunAndGu(parentRegion, childRegion)
        : regionIds;
  }
}

package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todayit.course.exception.InvalidRegionPathException;
import com.todayit.course.repository.RegionRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegionQueryServiceTest {

  @Mock private RegionRepository regionRepository;

  @Test
  @DisplayName("시·도만 선택하면 모든 하위 지역을 반환한다")
  void findsAllRegionsInSi() {
    // Given
    when(regionRepository.findIdsBySi("서울특별시")).thenReturn(List.of(1, 2));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds("서울특별시");

    // Then
    assertThat(result).containsExactly(1, 2);
    verify(regionRepository).findIdsBySi("서울특별시");
  }

  @Test
  @DisplayName("시·군만 선택하면 모든 하위 구를 반환한다")
  void findsAllRegionsInGun() {
    // Given
    when(regionRepository.findIdsBySi("수원시")).thenReturn(List.of());
    when(regionRepository.findIdsByGun("수원시")).thenReturn(List.of(10, 11, 12, 13));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds("수원시");

    // Then
    assertThat(result).containsExactly(10, 11, 12, 13);
    verify(regionRepository).findIdsBySi("수원시");
    verify(regionRepository).findIdsByGun("수원시");
  }

  @Test
  @DisplayName("구만 선택하면 동일한 이름의 구를 모두 반환한다")
  void findsAllRegionsInGu() {
    // Given
    when(regionRepository.findIdsBySi("중구")).thenReturn(List.of());
    when(regionRepository.findIdsByGun("중구")).thenReturn(List.of());
    when(regionRepository.findIdsByGu("중구")).thenReturn(List.of(20, 21));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds("중구");

    // Then
    assertThat(result).containsExactly(20, 21);
    verify(regionRepository).findIdsBySi("중구");
    verify(regionRepository).findIdsByGun("중구");
    verify(regionRepository).findIdsByGu("중구");
  }

  @Test
  @DisplayName("시·도와 구를 선택하면 해당 하위 지역만 반환한다")
  void findsRegionBySiAndSubdivision() {
    // Given
    when(regionRepository.findIdsBySiAndSubdivision("서울특별시", "동대문구")).thenReturn(List.of(10));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds(" 서울특별시   동대문구 ");

    // Then
    assertThat(result).containsExactly(10);
    verify(regionRepository).findIdsBySiAndSubdivision("서울특별시", "동대문구");
  }

  @Test
  @DisplayName("시·군과 구만 선택해도 해당 지역을 반환한다")
  void findsRegionByGunAndGu() {
    // Given
    when(regionRepository.findIdsBySiAndSubdivision("수원시", "팔달구")).thenReturn(List.of());
    when(regionRepository.findIdsByGunAndGu("수원시", "팔달구")).thenReturn(List.of(15));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds("수원시 팔달구");

    // Then
    assertThat(result).containsExactly(15);
    verify(regionRepository).findIdsBySiAndSubdivision("수원시", "팔달구");
    verify(regionRepository).findIdsByGunAndGu("수원시", "팔달구");
  }

  @Test
  @DisplayName("시·도, 시·군과 구를 순서대로 검증한다")
  void findsRegionByFullPath() {
    // Given
    when(regionRepository.findIdsBySiAndGunAndGu("경기도", "수원시", "팔달구")).thenReturn(List.of(20));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds("경기도 수원시 팔달구");

    // Then
    assertThat(result).containsExactly(20);
    verify(regionRepository).findIdsBySiAndGunAndGu("경기도", "수원시", "팔달구");
  }

  @Test
  @DisplayName("존재하지 않는 상하위 지역 조합은 거부한다")
  void rejectsInvalidRegionPath() {
    // Given
    when(regionRepository.findIdsBySiAndSubdivision("서울특별시", "해운대구")).thenReturn(List.of());
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds("서울특별시 해운대구"))
        .isInstanceOf(InvalidRegionPathException.class)
        .hasMessage("지역 필터 값이 올바르지 않습니다.");
  }
}

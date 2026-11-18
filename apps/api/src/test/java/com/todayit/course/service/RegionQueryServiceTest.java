package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayit.course.entity.Region;
import com.todayit.course.exception.InvalidRegionPathException;
import com.todayit.course.repository.RegionRepository;
import com.todayit.course.service.model.CourseRegionResult;
import com.todayit.course.service.model.RegionFilter;
import com.todayit.course.service.model.RegionResult;
import com.todayit.course.service.model.SigunguResult;
import com.todayit.course.service.model.SubRegionResult;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegionQueryServiceTest {
  @Mock private RegionRepository regionRepository;

  @Test
  @DisplayName("전체 지역 엔티티 목록을 시도 및 하위 시군구 계층 구조로 변환하여 조회한다")
  void findsAllRegionsHierarchically() {
    // Given
    List<Region> regions =
        List.of(
            new Region(1, "11", "110", "서울특별시", "종로구"),
            new Region(2, "11", "680", "서울특별시", "강남구"),
            new Region(3, "26", "110", "부산광역시", "중구"));
    when(regionRepository.findAllByOrderBySidoCodeAscSigunguCodeAsc()).thenReturn(regions);
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<RegionResult> result = service.findAllRegions();

    // Then
    assertThat(result).hasSize(2);

    RegionResult seoul = result.get(0);
    assertThat(seoul.sidoCode()).isEqualTo("11");
    assertThat(seoul.sidoName()).isEqualTo("서울특별시");
    assertThat(seoul.sigungus())
        .containsExactly(new SigunguResult("110", "종로구"), new SigunguResult("680", "강남구"));

    RegionResult busan = result.get(1);
    assertThat(busan.sidoCode()).isEqualTo("26");
    assertThat(busan.sidoName()).isEqualTo("부산광역시");
    assertThat(busan.sigungus()).containsExactly(new SigunguResult("110", "중구"));
  }

  @Test
  @DisplayName("세부 지역이 있는 시군구는 공백을 기준으로 3단계 계층 구조로 그룹화하여 조회한다")
  void findsAllRegionsInThreeDepthHierarchy() {
    // Given
    List<Region> regions =
        List.of(
            new Region(1, "41", "110", "경기도", "수원시"),
            new Region(2, "41", "111", "경기도", "수원시 장안구"),
            new Region(3, "41", "113", "경기도", "수원시 권선구"),
            new Region(4, "41", "130", "경기도", "성남시"),
            new Region(5, "41", "131", "경기도", "성남시 수정구"),
            new Region(6, "41", "135", "경기도", "성남시 분당구"),
            new Region(7, "41", "150", "경기도", "의정부시"));
    when(regionRepository.findAllByOrderBySidoCodeAscSigunguCodeAsc()).thenReturn(regions);
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<RegionResult> result = service.findAllRegions();

    // Then
    assertThat(result).hasSize(1);
    RegionResult gyeonggi = result.get(0);
    assertThat(gyeonggi.sidoCode()).isEqualTo("41");
    assertThat(gyeonggi.sidoName()).isEqualTo("경기도");
    assertThat(gyeonggi.sigungus()).hasSize(3);

    SigunguResult suwon = gyeonggi.sigungus().get(0);
    assertThat(suwon.sigunguCode()).isEqualTo("110");
    assertThat(suwon.sigunguName()).isEqualTo("수원시");
    assertThat(suwon.subRegions())
        .containsExactly(new SubRegionResult("111", "장안구"), new SubRegionResult("113", "권선구"));

    SigunguResult seongnam = gyeonggi.sigungus().get(1);
    assertThat(seongnam.sigunguCode()).isEqualTo("130");
    assertThat(seongnam.sigunguName()).isEqualTo("성남시");
    assertThat(seongnam.subRegions())
        .containsExactly(new SubRegionResult("131", "수정구"), new SubRegionResult("135", "분당구"));

    SigunguResult uijeongbu = gyeonggi.sigungus().get(2);
    assertThat(uijeongbu.sigunguCode()).isEqualTo("150");
    assertThat(uijeongbu.sigunguName()).isEqualTo("의정부시");
    assertThat(uijeongbu.subRegions()).isEmpty();
  }

  @Test
  @DisplayName("지역 엔티티 데이터가 비어있으면 빈 목록을 반환한다")
  void returnsEmptyListWhenNoRegions() {
    // Given
    when(regionRepository.findAllByOrderBySidoCodeAscSigunguCodeAsc()).thenReturn(List.of());
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<RegionResult> result = service.findAllRegions();

    // Then
    assertThat(result).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({"41,", ",115", "41,115", ","})
  @DisplayName("전달된 시도 또는 시군구 코드로 지역 식별자를 조회한다")
  void findsRegionsByProvidedCodes(String sidoCode, String sigunguCode) {
    // Given
    when(regionRepository.findIdsByCodes(sidoCode, sigunguCode)).thenReturn(List.of(1, 2));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds(sidoCode, sigunguCode);

    // Then
    assertThat(result).containsExactly(1, 2);
  }

  @ParameterizedTest
  @CsvSource({"1,", "411,", "ab,", "' ',", "'',", ",11", ",1100", ",abc", ",' '", ",''"})
  @DisplayName("잘못된 형식의 지역 코드는 조회 전에 예외를 던진다")
  void rejectsMalformedCodesBeforeQuerying(String sidoCode, String sigunguCode) {
    // Given
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds(sidoCode, sigunguCode))
        .isInstanceOf(InvalidRegionPathException.class);
    verifyNoInteractions(regionRepository);
  }

  @Test
  @DisplayName("조회된 지역 식별자가 없으면 예외를 던진다")
  void throwsExceptionWhenNoRegionIdsFound() {
    // Given
    when(regionRepository.findIdsByCodes("41", "115")).thenReturn(List.of());
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds("41", "115"))
        .isInstanceOf(InvalidRegionPathException.class);
  }

  @Test
  @DisplayName("단일 지역 코스로 요약 정보를 반환한다")
  void getsSummaryForSingleRegion() {
    // Given
    List<Region> regions = List.of(new Region(1, "11", "110", "서울특별시", "종로구"));
    when(regionRepository.findAllById(List.of(1))).thenReturn(regions);
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    CourseRegionResult result = service.getCourseRegionSummary(List.of(1));

    // Then
    assertThat(result).isNotNull();
    assertThat(result.sidoCode()).isEqualTo("11");
    assertThat(result.sidoName()).isEqualTo("서울특별시");
    assertThat(result.sigunguCode()).isEqualTo("110");
    assertThat(result.sigunguName()).isEqualTo("종로구");
  }

  @Test
  @DisplayName("여러 시군구가 포함된 경우 시군구 정보 없이 시도만 반환한다")
  void getsSummaryForMultipleSigungusInSameSido() {
    // Given
    List<Region> regions =
        List.of(
            new Region(1, "11", "110", "서울특별시", "종로구"), new Region(2, "11", "680", "서울특별시", "강남구"));
    when(regionRepository.findAllById(List.of(1, 2))).thenReturn(regions);
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    CourseRegionResult result = service.getCourseRegionSummary(List.of(1, 2));

    // Then
    assertThat(result).isNotNull();
    assertThat(result.sidoCode()).isEqualTo("11");
    assertThat(result.sidoName()).isEqualTo("서울특별시");
    assertThat(result.sigunguCode()).isNull();
    assertThat(result.sigunguName()).isNull();
  }

  @Test
  @DisplayName("전달된 지역 ID 목록이 null이거나 비어있으면 null을 반환한다")
  void returnsNullWhenRegionIdsEmpty() {
    // Given
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    CourseRegionResult resultNull = service.getCourseRegionSummary(null);
    CourseRegionResult resultEmpty = service.getCourseRegionSummary(List.of());

    // Then
    assertThat(resultNull).isNull();
    assertThat(resultEmpty).isNull();
  }

  @Test
  @DisplayName("지역 엔티티를 찾을 수 없으면 null을 반환한다")
  void returnsNullWhenNoRegionsFoundForSummary() {
    // Given
    when(regionRepository.findAllById(List.of(999))).thenReturn(List.of());
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    CourseRegionResult result = service.getCourseRegionSummary(List.of(999));

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("유효한 지역 필터 목록으로 지역 식별자 목록을 조회한다")
  void findsRegionIdsWithValidFilters() {
    // Given
    List<RegionFilter> filters =
        List.of(new RegionFilter("11", List.of("110", "680")), new RegionFilter("41", List.of()));
    List<Region> seoulRegions =
        List.of(
            new Region(1, "11", "110", "서울특별시", "종로구"), new Region(2, "11", "680", "서울특별시", "강남구"));
    when(regionRepository.findBySidoCodeAndSigunguCodeIn("11", List.of("110", "680")))
        .thenReturn(seoulRegions);
    when(regionRepository.findIdsBySidoCode("41")).thenReturn(List.of(10, 11));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> result = service.findRegionIds(filters);

    // Then
    assertThat(result).containsExactly(1, 2, 10, 11);
  }

  @ParameterizedTest
  @CsvSource({"190", "190:192"})
  @DisplayName("부천시 선택 시 하위 구를 포함하고 중복 선택한 구는 한 번만 반환한다")
  void includesSubRegionsWhenCitySelected(String codes) {
    List<String> selectedCodes = List.of(codes.split(":"));
    Region city = new Region(10, "41", "190", "경기도", "부천시");
    Region wonmi = new Region(11, "41", "192", "경기도", "부천시 원미구");
    when(regionRepository.findBySidoCodeAndSigunguCodeIn("41", selectedCodes))
        .thenReturn(selectedCodes.size() == 1 ? List.of(city) : List.of(city, wonmi));
    when(regionRepository.findSubRegionIds("41", "부천시")).thenReturn(List.of(11, 12, 13));
    RegionQueryService service = new RegionQueryService(regionRepository);

    List<Integer> result = service.findRegionIds(List.of(new RegionFilter("41", selectedCodes)));

    assertThat(result).containsExactly(10, 11, 12, 13);
  }

  @Test
  @DisplayName("하위 구만 선택하면 상위 시나 다른 구를 포함하지 않는다")
  void keepsSelectedDistrictOnly() {
    when(regionRepository.findBySidoCodeAndSigunguCodeIn("41", List.of("192")))
        .thenReturn(List.of(new Region(11, "41", "192", "경기도", "부천시 원미구")));
    RegionQueryService service = new RegionQueryService(regionRepository);

    List<Integer> result = service.findRegionIds(List.of(new RegionFilter("41", List.of("192"))));

    assertThat(result).containsExactly(11);
    verify(regionRepository, never()).findSubRegionIds(anyString(), anyString());
  }

  @Test
  @DisplayName("지역 필터 목록이 null이거나 비어있으면 빈 목록을 반환한다")
  void returnsEmptyListWhenFiltersEmpty() {
    // Given
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When
    List<Integer> resultNull = service.findRegionIds((List<RegionFilter>) null);
    List<Integer> resultEmpty = service.findRegionIds(List.of());

    // Then
    assertThat(resultNull).isEmpty();
    assertThat(resultEmpty).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({"1", "411", "ab", "' '", "''"})
  @DisplayName("필터의 시도 코드가 잘못된 형식인 경우 예외를 던진다")
  void rejectsInvalidSidoCodeInFilters(String invalidSidoCode) {
    // Given
    List<RegionFilter> filters = List.of(new RegionFilter(invalidSidoCode, List.of()));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds(filters))
        .isInstanceOf(InvalidRegionPathException.class);
  }

  @ParameterizedTest
  @CsvSource({"11", "1100", "abc", "' '", "''"})
  @DisplayName("필터의 시군구 코드가 잘못된 형식인 경우 예외를 던진다")
  void rejectsInvalidSigunguCodeInFilters(String invalidSigunguCode) {
    // Given
    List<RegionFilter> filters = List.of(new RegionFilter("11", List.of(invalidSigunguCode)));
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds(filters))
        .isInstanceOf(InvalidRegionPathException.class);
  }

  @Test
  @DisplayName("시군구 목록이 비어있을 때 해당 시도의 지역이 없으면 예외를 던진다")
  void throwsExceptionWhenSidoNotFoundInFilters() {
    // Given
    List<RegionFilter> filters = List.of(new RegionFilter("99", List.of()));
    when(regionRepository.findIdsBySidoCode("99")).thenReturn(List.of());
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds(filters))
        .isInstanceOf(InvalidRegionPathException.class);
  }

  @Test
  @DisplayName("일부 시군구가 조회되지 않으면 예외를 던진다")
  void throwsExceptionWhenSomeSigunguNotFoundInFilters() {
    // Given
    List<RegionFilter> filters = List.of(new RegionFilter("11", List.of("110", "999")));
    List<Region> matchedRegions = List.of(new Region(1, "11", "110", "서울특별시", "종로구"));
    when(regionRepository.findBySidoCodeAndSigunguCodeIn("11", List.of("110", "999")))
        .thenReturn(matchedRegions);
    RegionQueryService service = new RegionQueryService(regionRepository);

    // When & Then
    assertThatThrownBy(() -> service.findRegionIds(filters))
        .isInstanceOf(InvalidRegionPathException.class);
  }
}

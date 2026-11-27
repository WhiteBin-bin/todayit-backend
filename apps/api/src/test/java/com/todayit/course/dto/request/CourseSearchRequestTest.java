package com.todayit.course.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.common.exception.InvalidPaginationException;
import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.command.CourseSearchCommand;
import com.todayit.course.service.model.CourseDurationFilter;
import com.todayit.course.service.model.CourseSort;
import com.todayit.region.exception.RegionException;
import com.todayit.region.service.model.RegionFilter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CourseSearchRequestTest {
  @Test
  @DisplayName("장소 필터와 같은 시도·시군구 입력으로 여러 지역을 조회한다")
  void acceptsSidoAndSigunguFiltersLikePlaceSearch() {
    // given
    String regions =
        """
        [{"sidoCode":"11","sigunguCodes":["680","650"]},{"sidoCode":"41","sigunguCodes":[]}]
        """;
    CourseSearchRequest request =
        new CourseSearchRequest(regions, null, null, null, null, null, null);

    // when
    CourseSearchCommand command = request.toCommand();

    // then
    assertThat(command.regions())
        .containsExactly(
            new RegionFilter("11", List.of("680", "650")), new RegionFilter("41", List.of()));
  }

  @Test
  @DisplayName("필터를 모두 생략하면 기본 페이지(0), 크기(20), 정렬(LATEST)로 변환한다")
  void convertsDefaultParametersSuccessfully() {
    // given
    CourseSearchRequest request = new CourseSearchRequest(null, null, null, null, null, null, null);

    // when
    CourseSearchCommand command = request.toCommand();

    // then
    assertThat(command.regions()).isEmpty();
    assertThat(command.themes()).isEmpty();
    assertThat(command.duration()).isEqualTo(CourseDurationFilter.ALL);
    assertThat(command.transport()).isNull();
    assertThat(command.sort()).isEqualTo(CourseSort.LATEST);
    assertThat(command.page()).isZero();
    assertThat(command.size()).isEqualTo(20);
  }

  @ParameterizedTest
  @CsvSource({"CAR, CAR", "WALKING, WALKING", "PUBLIC_TRANSPORT, PUBLIC_TRANSPORT"})
  @DisplayName("regions JSON 문자열과 이동수단 및 정렬을 정상적으로 매핑한다")
  void mapsRegionsJsonTransportAndSortSuccessfully(
      String rawTransport, CourseTransport expectedTransport) {
    // given
    String regionsJson =
        "[{\"sidoCode\":\"11\",\"sigunguCodes\":[\"680\",\"650\"]},{\"sidoCode\":\"41\",\"sigunguCodes\":[]}]";
    CourseSearchRequest request =
        new CourseSearchRequest(
            regionsJson,
            List.of("로맨틱", "아늑함"),
            CourseDurationFilter.TWO_TO_FOUR_HOURS,
            rawTransport,
            "POPULAR",
            1,
            10);

    // when
    CourseSearchCommand command = request.toCommand();

    // then
    assertThat(command.regions())
        .containsExactly(
            new RegionFilter("11", List.of("680", "650")), new RegionFilter("41", List.of()));
    assertThat(command.themes()).containsExactly("로맨틱", "아늑함");
    assertThat(command.duration()).isEqualTo(CourseDurationFilter.TWO_TO_FOUR_HOURS);
    assertThat(command.transport()).isEqualTo(expectedTransport);
    assertThat(command.sort()).isEqualTo(CourseSort.POPULAR);
    assertThat(command.page()).isEqualTo(1);
    assertThat(command.size()).isEqualTo(10);
  }

  @Test
  @DisplayName("끝자리가 0이 아닌 하위 구 코드는 시도당 3개 제한에서 제외하고 허용한다")
  void allowsSubRegionCodesExceedingThreeWhenNotEndingWithZero() {
    // given
    String regionsJson =
        "[{\"sidoCode\":\"41\",\"sigunguCodes\":[\"111\",\"113\",\"115\",\"117\",\"130\",\"280\"]}]";
    CourseSearchRequest request =
        new CourseSearchRequest(regionsJson, null, null, null, null, null, null);

    // when
    CourseSearchCommand command = request.toCommand();

    // then
    assertThat(command.regions())
        .containsExactly(new RegionFilter("41", List.of("111", "113", "115", "117", "130", "280")));
  }

  @Test
  @DisplayName("끝자리가 0인 시군구 코드가 시도당 3개를 초과하면 RegionException을 던진다")
  void throwsExceptionWhenSigunguEndingWithZeroExceedsThree() {
    // given
    String regionsJson =
        "[{\"sidoCode\":\"11\",\"sigunguCodes\":[\"110\",\"140\",\"170\",\"200\"]}]";
    CourseSearchRequest request =
        new CourseSearchRequest(regionsJson, null, null, null, null, null, null);

    // when
    RegionException exception = assertThrows(RegionException.class, request::toCommand);

    // then
    assertThat(exception.getErrorCode().code()).isEqualTo("EXCEEDED_SIGUNGU_LIMIT");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "",
        "   ",
        "[]",
        "invalid-json",
        "[{\"sidoCode\":\"1\"}]",
        "[{\"sidoCode\":\"11\",\"sigunguCodes\":[\"12\"]}]",
        "[{\"sidoCode\":\"11\"},{\"sidoCode\":\"11\"}]"
      })
  @DisplayName("regions JSON 형식이 잘못되었거나 중복 시도·잘못된 코드이면 RegionException을 던진다")
  void throwsExceptionWhenRegionsJsonIsInvalid(String invalidRegionsJson) {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(invalidRegionsJson, null, null, null, null, null, null);

    // when
    RegionException exception = assertThrows(RegionException.class, request::toCommand);

    // then
    assertThat(exception.getErrorCode().code()).isEqualTo("INVALID_REGION_PATH");
  }

  @Test
  @DisplayName("콘셉트가 2개를 초과하면 InvalidRequestException을 던진다")
  void throwsExceptionWhenThemesExceedMaxCount() {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(null, List.of("로맨틱", "아늑함", "감성적"), null, null, null, null, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception.getMessage()).isEqualTo("콘셉트는 중복 없이 최대 2개까지 입력할 수 있습니다.");
  }

  @Test
  @DisplayName("콘셉트에 중복이 있으면 InvalidRequestException을 던진다")
  void throwsExceptionWhenThemesContainDuplicates() {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(null, List.of("로맨틱", "로맨틱"), null, null, null, null, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception.getMessage()).isEqualTo("콘셉트는 중복 없이 최대 2개까지 입력할 수 있습니다.");
  }

  @Test
  @DisplayName("콘셉트에 공백이 포함되면 InvalidRequestException을 던진다")
  void throwsExceptionWhenThemesContainBlank() {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(null, List.of("  "), null, null, null, null, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception.getMessage()).isEqualTo("콘셉트는 중복 없이 최대 2개까지 입력할 수 있습니다.");
  }

  @ParameterizedTest
  @CsvSource({
    "ALL, ALL",
    "UNDER_2_HOURS, UNDER_2_HOURS",
    "TWO_TO_FOUR_HOURS, TWO_TO_FOUR_HOURS",
    "OVER_4_HOURS, OVER_4_HOURS"
  })
  @DisplayName("소요 시간 필터 enum 값을 단일 구간으로 매핑한다")
  void mapsDurationFilterSuccessfully(
      CourseDurationFilter rawDuration, CourseDurationFilter expectedDuration) {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(null, null, rawDuration, null, null, null, null);

    // when
    CourseSearchCommand command = request.toCommand();

    // then
    assertThat(command.duration()).isEqualTo(expectedDuration);
  }

  @Test
  @DisplayName("지원하지 않는 이동수단이면 InvalidRequestException을 던진다")
  void throwsExceptionWhenTransportIsUnsupported() {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(null, null, null, "PLANE", null, null, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception.getMessage()).isEqualTo("지원하지 않는 이동수단입니다.");
  }

  @Test
  @DisplayName("지원하지 않는 정렬 기준이면 InvalidRequestException을 던진다")
  void throwsExceptionWhenSortIsUnsupported() {
    // given
    CourseSearchRequest request =
        new CourseSearchRequest(null, null, null, null, "OLDEST", null, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception.getMessage()).isEqualTo("지원하지 않는 정렬 기준입니다.");
  }

  @Test
  @DisplayName("페이지 번호가 음수이면 InvalidPaginationException을 던진다")
  void throwsExceptionWhenPageIsNegative() {
    // given
    CourseSearchRequest request = new CourseSearchRequest(null, null, null, null, null, -1, 20);

    // when
    InvalidPaginationException exception =
        assertThrows(InvalidPaginationException.class, request::toCommand);

    // then
    assertThat(exception.getMessage()).isNotBlank();
  }
}

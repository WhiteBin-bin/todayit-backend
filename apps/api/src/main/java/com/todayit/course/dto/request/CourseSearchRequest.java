package com.todayit.course.dto.request;

import com.todayit.common.exception.InvalidRequestException;
import com.todayit.common.pagination.PaginationValidator;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.command.CourseSearchCommand;
import com.todayit.course.service.model.CourseDurationFilter;
import com.todayit.course.service.model.CourseSort;
import com.todayit.region.RegionFilterParser;
import com.todayit.region.exception.RegionException;
import com.todayit.region.service.model.RegionFilter;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.HashSet;
import java.util.List;

/**
 * 코스 목록 및 필터 조회 요청입니다.
 *
 * @param regions 지역 필터 JSON 배열 문자열, 생략 가능
 * @param themes 콘셉트 이름 목록, 최대 2개
 * @param duration 소요 시간 구간 필터 (ALL, UNDER_2_HOURS, TWO_TO_FOUR_HOURS, OVER_4_HOURS), 기본값 ALL
 * @param transport CAR, WALK, PUBLIC_TRANSIT 또는 기존 이동수단 코드
 * @param sort 정렬 기준 (LATEST 또는 POPULAR), 기본값 LATEST
 * @param page 페이지 번호, 기본값 0
 * @param size 페이지 크기, 기본값 20
 */
public record CourseSearchRequest(
    @Parameter(
            description =
                "지역 필터 JSON 배열 문자열입니다. regions를 생략하면 지역 필터를 적용하지 않습니다. "
                    + "각 시도 항목의 sigunguCodes를 생략하거나 빈 배열로 전달하면 해당 시도 전체를 조회합니다. "
                    + "끝자리가 0인 시군구 코드만 시도당 최대 3개로 제한하며 나머지 코드는 개수 제한에서 제외합니다.",
            example =
                "[{\"sidoCode\":\"11\",\"sigunguCodes\":[\"680\",\"650\"]},{\"sidoCode\":\"41\",\"sigunguCodes\":[]}]")
        String regions,
    @Parameter(description = "콘셉트 이름 목록입니다. 중복 없이 최대 2개까지 선택할 수 있습니다.") List<String> themes,
    @Parameter(
            description =
                "소요 시간 구간 필터입니다. 단일 선택이며 생략 시 전체(ALL)를 조회합니다. "
                    + "(ALL: 전체, UNDER_2_HOURS: 2시간 이하, TWO_TO_FOUR_HOURS: 2~4시간, OVER_4_HOURS: 4시간 초과)",
            example = "TWO_TO_FOUR_HOURS")
        CourseDurationFilter duration,
    @Parameter(description = "이동수단 필터입니다. (CAR, WALK, PUBLIC_TRANSIT)", example = "WALK")
        String transport,
    @Parameter(description = "정렬 기준입니다. (LATEST: 최신순, POPULAR: 인기순)", example = "LATEST")
        String sort,
    @Parameter(description = "페이지 번호입니다. 0부터 시작합니다.", example = "0") Integer page,
    @Parameter(description = "페이지 크기입니다. 기본값은 20입니다.", example = "20") Integer size) {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;

  /**
   * 조회 조건과 페이지 파라미터를 검증하고 업무 명령으로 변환합니다.
   *
   * @return 검증된 코스 목록·필터 조회 명령
   * @throws RegionException 지역 필터 JSON 형식이 잘못되었거나 시군구 개수 제한을 초과한 경우
   * @throws InvalidRequestException 콘셉트·시간·이동수단·정렬 형식이 유효하지 않은 경우
   */
  public CourseSearchCommand toCommand() {
    int resolvedPage = page == null ? DEFAULT_PAGE : page;
    int resolvedSize = size == null ? DEFAULT_SIZE : size;
    PaginationValidator.validate(resolvedPage, resolvedSize);

    List<RegionFilter> regionFilters = parseRegionFilters();

    List<String> selectedThemes = themes == null ? List.of() : themes;
    if (selectedThemes.size() > 2
        || selectedThemes.stream().anyMatch(t -> t == null || t.isBlank())
        || new HashSet<>(selectedThemes).size() != selectedThemes.size()) {
      throw new InvalidRequestException("콘셉트는 중복 없이 최대 2개까지 입력할 수 있습니다.");
    }

    CourseDurationFilter selectedDuration = duration == null ? CourseDurationFilter.ALL : duration;

    CourseTransport selectedTransport = null;
    if (transport != null) {
      selectedTransport =
          switch (transport) {
            case "CAR" -> CourseTransport.CAR;
            case "WALK", "WALKING" -> CourseTransport.WALKING;
            case "PUBLIC_TRANSIT", "PUBLIC_TRANSPORT" -> CourseTransport.PUBLIC_TRANSPORT;
            default -> throw new InvalidRequestException("지원하지 않는 이동수단입니다.");
          };
    }

    CourseSort selectedSort = CourseSort.LATEST;
    if (sort != null) {
      selectedSort =
          switch (sort) {
            case "LATEST" -> CourseSort.LATEST;
            case "POPULAR" -> CourseSort.POPULAR;
            default -> throw new InvalidRequestException("지원하지 않는 정렬 기준입니다.");
          };
    }

    return new CourseSearchCommand(
        regionFilters,
        selectedThemes,
        selectedDuration,
        selectedTransport,
        selectedSort,
        resolvedPage,
        resolvedSize);
  }

  private List<RegionFilter> parseRegionFilters() {
    return RegionFilterParser.parse(regions).stream()
        .map(value -> new RegionFilter(value.sidoCode(), value.sigunguCodes()))
        .toList();
  }
}

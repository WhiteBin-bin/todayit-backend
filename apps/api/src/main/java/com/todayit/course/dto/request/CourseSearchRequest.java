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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
                """
                지역 필터 JSON 배열 문자열입니다.

                - 생략하면 지역 필터를 적용하지 않습니다.
                - 빈 배열(`[]`), 빈 문자열 또는 공백 문자열을 전달하면 `400 INVALID_REGION_PATH`를 반환합니다.
                - 각 시도 항목의 `sigunguCodes`를 생략하거나 빈 배열(`[]`)로 전달하면 해당 시도 전체를 조회합니다.
                - 끝자리가 `0`인 시군구 코드만 시도당 최대 3개로 제한하며 나머지 코드는 개수 제한에서 제외합니다.
                - 시군구 코드를 전달하면 해당 코드의 지역만 조회하며 하위 구 코드는 자동으로 추가하지 않습니다.
                """,
            example =
                "[{\"sidoCode\":\"11\",\"sigunguCodes\":[\"680\",\"650\"]},{\"sidoCode\":\"41\",\"sigunguCodes\":[]}]")
        String regions,
    @Parameter(
            description =
                """
                코스 콘셉트 필터 목록입니다.

                - 중복 없이 최대 2개까지 복수 선택할 수 있습니다.
                - 선택한 콘셉트 중 하나에 해당하는 코스를 조회합니다.
                - 생략하면 전체 콘셉트를 조회합니다.
                - 허용 콘셉트: `조용함`, `로맨틱`, `아늑함`, `감성적`, `활기참`, `이색적`, `여유로움`, `고급스러움`, `레트로`, `자연친화적`
                """,
            examples = {
              @ExampleObject(name = "조용함", value = "[\"조용함\"]"),
              @ExampleObject(name = "로맨틱", value = "[\"로맨틱\"]"),
              @ExampleObject(name = "아늑함", value = "[\"아늑함\"]"),
              @ExampleObject(name = "감성적", value = "[\"감성적\"]"),
              @ExampleObject(name = "활기참", value = "[\"활기참\"]"),
              @ExampleObject(name = "이색적", value = "[\"이색적\"]"),
              @ExampleObject(name = "여유로움", value = "[\"여유로움\"]"),
              @ExampleObject(name = "고급스러움", value = "[\"고급스러움\"]"),
              @ExampleObject(name = "레트로", value = "[\"레트로\"]"),
              @ExampleObject(name = "자연친화적", value = "[\"자연친화적\"]"),
              @ExampleObject(name = "로맨틱 + 아늑함", value = "[\"로맨틱\",\"아늑함\"]"),
              @ExampleObject(name = "감성적 + 여유로움", value = "[\"감성적\",\"여유로움\"]"),
              @ExampleObject(name = "자연친화적 + 조용함", value = "[\"자연친화적\",\"조용함\"]")
            },
            array =
                @ArraySchema(
                    schema =
                        @Schema(
                            type = "string",
                            allowableValues = {
                              "조용함", "로맨틱", "아늑함", "감성적", "활기참", "이색적", "여유로움", "고급스러움", "레트로",
                              "자연친화적"
                            })))
        List<String> themes,
    @Parameter(
            description =
                """
                소요 시간 구간 필터입니다.

                - 단일 선택이며 생략하면 `ALL`(전체)을 조회합니다.
                - `ALL`: 전체 (시간 필터 미적용)
                - `UNDER_2_HOURS`: 2시간 이하 (120분 이하)
                - `TWO_TO_FOUR_HOURS`: 2~4시간 (120분 초과 ~ 240분 이하)
                - `OVER_4_HOURS`: 4시간 초과 (240분 초과)
                """,
            example = "TWO_TO_FOUR_HOURS",
            schema = @Schema(defaultValue = "ALL"))
        CourseDurationFilter duration,
    @Parameter(
            description =
                """
                이동수단 필터입니다.

                - 생략하면 전체 이동수단을 조회합니다.
                - `CAR`: 자동차
                - `WALK`: 도보
                - `PUBLIC_TRANSIT`: 대중교통
                """,
            example = "WALK",
            schema = @Schema(allowableValues = {"CAR", "WALK", "PUBLIC_TRANSIT"}))
        String transport,
    @Parameter(
            description =
                """
                정렬 기준입니다.

                - 생략하면 기본값은 `LATEST`입니다.
                - `LATEST`: 최신순
                - `POPULAR`: 인기순 (조회수순)
                """,
            example = "LATEST",
            schema =
                @Schema(
                    allowableValues = {"LATEST", "POPULAR"},
                    defaultValue = "LATEST"))
        String sort,
    @Parameter(
            description =
                """
                페이지 번호입니다.

                - 0부터 시작합니다.
                - 생략하면 기본값은 `0`입니다.
                """,
            example = "0",
            schema = @Schema(minimum = "0", defaultValue = "0"))
        Integer page,
    @Parameter(
            description =
                """
                페이지당 결과 개수입니다.

                - 1부터 100까지 입력할 수 있습니다.
                - 생략하면 기본값은 `20`입니다.
                """,
            example = "20",
            schema = @Schema(minimum = "1", maximum = "100", defaultValue = "20"))
        Integer size) {

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

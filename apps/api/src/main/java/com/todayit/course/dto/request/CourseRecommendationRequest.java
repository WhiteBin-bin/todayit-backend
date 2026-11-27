package com.todayit.course.dto.request;

import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.command.CourseRecommendationCommand;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.List;

/**
 * 추천 코스 조회 필터입니다.
 *
 * @param region 시·도 2자리 또는 시·도와 시·군·구 5자리 코드
 * @param themes 콘셉트 이름, 최대 2개
 * @param durationMinutes 총 예상시간 상한(분), 양수
 * @param transport CAR, WALK, PUBLIC_TRANSIT 또는 기존 이동수단 코드
 */
public record CourseRecommendationRequest(
    @Parameter(
            description =
                """
                행정 지역 코드 필터입니다.

                - 시·도 2자리(예: `11`) 또는 시·도+시·군·구 5자리(예: `11110`) 코드로 입력합니다.
                - 생략하면 전체 지역을 대상으로 추천합니다.
                """,
            example = "11110")
        String region,
    @Parameter(
            description =
                """
                코스 콘셉트 필터 목록입니다.

                - 중복 없이 최대 2개까지 복수 선택할 수 있습니다.
                - 생략하면 콘셉트 필터를 적용하지 않습니다.
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
                총 예상 소요 시간의 상한(분)입니다.

                - 1 이상의 양수로 입력합니다.
                - 입력 시 소요 시간이 확인되지 않은 코스는 추천 대상에서 제외됩니다.
                - 생략하면 소요 시간 제한을 두지 않습니다.
                """,
            example = "240",
            schema = @Schema(minimum = "1"))
        Integer durationMinutes,
    @Parameter(
            description =
                """
                이동수단 필터입니다.

                - 생략하면 전체 이동수단을 대상으로 추천합니다.
                - `CAR`: 자동차
                - `WALK`: 도보
                - `PUBLIC_TRANSIT`: 대중교통
                """,
            example = "WALK",
            schema = @Schema(allowableValues = {"CAR", "WALK", "PUBLIC_TRANSIT"}))
        String transport) {
  /**
   * 조회 조건을 검증하고 업무 명령으로 변환합니다.
   *
   * @return 검증된 추천 조회 조건
   * @throws InvalidRequestException 지역·콘셉트·시간·이동수단 형식이 유효하지 않은 경우
   */
  public CourseRecommendationCommand toCommand() {
    if (region != null && !region.matches("[0-9]{2}([0-9]{3})?")) {
      throw new InvalidRequestException("지역은 시·도 2자리 또는 시·도와 시·군·구 5자리 코드로 입력해야 합니다.");
    }
    List<String> selected = themes == null ? List.of() : themes;
    if (selected.size() > 2
        || selected.stream().anyMatch(t -> t == null || t.isBlank())
        || new HashSet<>(selected).size() != selected.size()) {
      throw new InvalidRequestException("콘셉트는 중복 없이 최대 2개까지 입력할 수 있습니다.");
    }
    if (durationMinutes != null && durationMinutes <= 0) {
      throw new InvalidRequestException("예상시간 상한은 양수이어야 합니다.");
    }
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
    return new CourseRecommendationCommand(region, selected, durationMinutes, selectedTransport);
  }
}

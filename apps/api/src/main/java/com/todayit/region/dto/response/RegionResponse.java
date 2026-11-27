package com.todayit.region.dto.response;

import com.todayit.region.service.model.RegionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 시·도 및 하위 시·군·구 목록을 포함하는 행정 지역 응답 DTO입니다.
 *
 * @param sidoCode 시·도 코드
 * @param sidoName 시·도 명칭
 * @param sigungus 하위 시·군·구 목록
 */
@Schema(description = "시·도 및 시·군·구 계층 지역 응답")
public record RegionResponse(
    @Schema(
            description =
                """
                시·도 법정동 코드입니다.

                - 2자리 숫자 문자열입니다. (예: `11` 서울특별시, `41` 경기도)
                """,
            example = "11",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sidoCode,
    @Schema(
            description = "시·도 명칭입니다.",
            example = "서울특별시",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sidoName,
    @Schema(
            description =
                """
                해당 시·도에 속한 시·군·구 목록입니다.

                - 끝자리가 `0`인 3자리 시·군·구 코드 기준 오름차순으로 정렬됩니다.
                """,
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<SigunguResponse> sigungus) {

  /**
   * 시·군·구 목록을 방어적으로 복사하여 초기화합니다.
   *
   * @param sidoCode 시·도 코드
   * @param sidoName 시·도 명칭
   * @param sigungus 하위 시·군·구 목록
   */
  public RegionResponse {
    sigungus = sigungus != null ? List.copyOf(sigungus) : List.of();
  }

  /**
   * 서비스 계층의 지역 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 지역 조회 결과 모델
   * @return 지역 응답 DTO
   */
  public static RegionResponse from(RegionResult result) {
    List<SigunguResponse> sigunguResponses =
        result.sigungus().stream().map(SigunguResponse::from).toList();
    return new RegionResponse(result.sidoCode(), result.sidoName(), sigunguResponses);
  }
}

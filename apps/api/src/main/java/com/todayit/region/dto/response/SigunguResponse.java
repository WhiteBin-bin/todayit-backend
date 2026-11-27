package com.todayit.region.dto.response;

import com.todayit.region.service.model.SigunguResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 시·군·구 정보 및 하위 세부 지역 응답 DTO입니다.
 *
 * @param sigunguCode 시·군·구 코드
 * @param sigunguName 시·군·구 명칭
 * @param subRegions 하위 세부 지역 목록
 */
@Schema(description = "시·군·구 응답")
public record SigunguResponse(
    @Schema(
            description =
                """
                시·군·구 법정동 코드입니다.

                - 끝자리가 `0`인 3자리 숫자 문자열입니다. (예: `110` 종로구/수원시, `130` 성남시)
                """,
            example = "130",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sigunguCode,
    @Schema(
            description = "시·군·구 명칭입니다.",
            example = "성남시",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sigunguName,
    @Schema(
            description =
                """
                하위 세부 지역(일반구) 목록입니다.

                - 수원시, 성남시 등 일반구를 가진 시(市)인 경우 하위 구 목록을 포함합니다.
                - 하위 일반구가 없는 시·군·구는 빈 배열(`[]`)을 반환합니다.
                """,
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<SubRegionResponse> subRegions) {

  /**
   * 세부 지역 목록을 방어적으로 복사하여 초기화합니다.
   *
   * @param sigunguCode 시·군·구 코드
   * @param sigunguName 시·군·구 명칭
   * @param subRegions 하위 세부 지역 목록
   */
  public SigunguResponse {
    subRegions = subRegions != null ? List.copyOf(subRegions) : List.of();
  }

  /**
   * 하위 세부 지역이 없는 시·군·구 응답 DTO를 생성합니다.
   *
   * @param sigunguCode 시·군·구 코드
   * @param sigunguName 시·군·구 명칭
   */
  public SigunguResponse(String sigunguCode, String sigunguName) {
    this(sigunguCode, sigunguName, List.of());
  }

  /**
   * 서비스 계층의 시·군·구 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 시·군·구 결과 모델
   * @return 시·군·구 응답 DTO
   */
  public static SigunguResponse from(SigunguResult result) {
    List<SubRegionResponse> subRegionResponses =
        result.subRegions().stream().map(SubRegionResponse::from).toList();
    return new SigunguResponse(result.sigunguCode(), result.sigunguName(), subRegionResponses);
  }
}

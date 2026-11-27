package com.todayit.region.dto.response;

import com.todayit.region.service.model.SubRegionResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 세부 지역 정보 응답 DTO입니다.
 *
 * @param sigunguCode 시·군·구 법정동 코드
 * @param sigunguName 세부 지역 명칭
 */
@Schema(description = "세부 지역(일반구) 응답")
public record SubRegionResponse(
    @Schema(
            description =
                """
                세부 지역(일반구)의 시·군·구 법정동 코드입니다.

                - 끝자리가 `0`이 아닌 3자리 숫자 문자열입니다. (예: `135` 분당구, `115` 팔달구)
                """,
            example = "135",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sigunguCode,
    @Schema(
            description = "세부 지역(일반구) 명칭입니다.",
            example = "분당구",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sigunguName) {

  /**
   * 서비스 계층의 세부 지역 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 세부 지역 결과 모델
   * @return 세부 지역 응답 DTO
   */
  public static SubRegionResponse from(SubRegionResult result) {
    return new SubRegionResponse(result.sigunguCode(), result.sigunguName());
  }
}

package com.todayit.course.dto.response;

import com.todayit.course.service.model.SubRegionResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 세부 지역 정보 응답 DTO입니다.
 *
 * @param sigunguCode 시·군·구 법정동 코드
 * @param sigunguName 세부 지역 명칭
 */
@Schema(description = "세부 지역 응답")
public record SubRegionResponse(
    @Schema(description = "시·군·구 법정동 코드 (3자리)", example = "135") String sigunguCode,
    @Schema(description = "세부 지역 명칭", example = "분당구") String sigunguName) {

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

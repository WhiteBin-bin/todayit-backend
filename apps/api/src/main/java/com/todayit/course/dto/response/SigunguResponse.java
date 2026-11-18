package com.todayit.course.dto.response;

import com.todayit.course.service.model.SigunguResult;
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
    @Schema(description = "시·군·구 법정동 코드 (3자리)", example = "130") String sigunguCode,
    @Schema(description = "시·군·구 명칭", example = "성남시") String sigunguName,
    @Schema(description = "하위 세부 지역 목록") List<SubRegionResponse> subRegions) {

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

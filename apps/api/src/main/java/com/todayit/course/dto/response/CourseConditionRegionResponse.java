package com.todayit.course.dto.response;

import com.todayit.region.service.model.RegionSummaryResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 생성 조건의 행정 지역 정보 응답 DTO입니다.
 *
 * @param sidoCode 시·도 코드
 * @param sidoName 시·도 명칭
 * @param sigunguCode 시·군·구 코드, 없으면 null
 * @param sigunguName 시·군·구 명칭, 없으면 null
 */
@Schema(description = "코스 행정 지역 조건 정보")
public record CourseConditionRegionResponse(
    @Schema(description = "시·도 법정동 코드 (2자리)", example = "11") String sidoCode,
    @Schema(description = "시·도 명칭", example = "서울특별시") String sidoName,
    @Schema(description = "시·군·구 법정동 코드 (3자리)", example = "110", nullable = true)
        String sigunguCode,
    @Schema(description = "시·군·구 명칭", example = "종로구", nullable = true) String sigunguName) {

  /**
   * 서비스 계층의 지역 요약 결과로부터 응답 DTO를 생성합니다.
   *
   * @param result 서비스 지역 요약 결과
   * @return 지역 응답 DTO, 전달된 결과가 null이면 null
   */
  public static CourseConditionRegionResponse from(RegionSummaryResult result) {
    if (result == null) {
      return null;
    }
    return new CourseConditionRegionResponse(
        result.sidoCode(), result.sidoName(), result.sigunguCode(), result.sigunguName());
  }
}

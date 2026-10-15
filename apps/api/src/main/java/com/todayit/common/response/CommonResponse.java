package com.todayit.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 성공 API 응답의 공통 형식입니다.
 *
 * @param <T> 응답 데이터 타입
 * @param success 요청 성공 여부
 * @param data 응답 데이터
 */
@Schema(description = "공통 성공 응답")
public record CommonResponse<T>(
    @Schema(
            description = "요청 성공 여부입니다.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean success,
    @Schema(description = "API 응답 데이터입니다.", requiredMode = Schema.RequiredMode.REQUIRED) T data) {

  /**
   * 성공 응답을 생성합니다.
   *
   * @param data 응답 데이터
   * @param <T> 응답 데이터 타입
   * @return 성공 API 응답
   */
  public static <T> CommonResponse<T> success(T data) {
    return new CommonResponse<>(true, data);
  }
}

package com.todayit.common.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;

/**
 * API에서 사용하는 페이지 응답입니다.
 *
 * @param <T> 응답 요소 타입
 * @param content 목록
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 * @param totalElements 전체 요소 수
 * @param totalPages 전체 페이지 수
 */
@Schema(description = "페이지 조회 응답")
public record PageResponse<T>(
    @Schema(
            description = "현재 페이지의 조회 결과 목록입니다. 결과가 없으면 빈 목록을 반환합니다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<T> content,
    @Schema(
            description = "현재 페이지 번호입니다. 첫 페이지는 0입니다.",
            example = "0",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int page,
    @Schema(
            description = "요청한 페이지 크기입니다.",
            example = "20",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int size,
    @Schema(
            description = "조회 조건에 해당하는 전체 항목 수입니다.",
            example = "42",
            requiredMode = Schema.RequiredMode.REQUIRED)
        long totalElements,
    @Schema(
            description = "페이지 크기를 기준으로 계산한 전체 페이지 수입니다.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int totalPages) {

  /** 페이지 응답의 목록을 방어적으로 복사합니다. */
  public PageResponse {
    content = List.copyOf(content);
  }

  /**
   * 서비스 페이지 결과를 API 페이지 응답으로 변환합니다.
   *
   * @param result 서비스 페이지 결과
   * @param mapper 요소 변환 함수
   * @param <T> 서비스 요소 타입
   * @param <R> API 응답 요소 타입
   * @return API 페이지 응답
   */
  public static <T, R> PageResponse<R> from(PageResult<T> result, Function<T, R> mapper) {
    return new PageResponse<>(
        result.content().stream().map(mapper).toList(),
        result.page(),
        result.size(),
        result.totalElements(),
        result.totalPages());
  }
}

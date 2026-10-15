package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.model.CourseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 코스 조회 API 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param title 코스명
 * @param courseSummary 코스 요약
 * @param transport 이동수단
 * @param startAt 코스 시작 시간
 * @param endAt 코스 종료 시간
 * @param viewCount 조회수
 */
@Schema(description = "코스 조회 응답")
public record CourseResponse(
    @Schema(description = "코스 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "코스 이름입니다.",
            example = "서울 도심 산책 코스",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
    @Schema(
            description = "코스 요약입니다. 요약이 없으면 null을 반환합니다.",
            example = "카페와 전시를 즐기는 도심 산책 코스",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String courseSummary,
    @Schema(
            description = "코스의 이동 수단입니다.",
            example = "WALKING",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseTransport transport,
    @Schema(
            description = "코스 시작 일시입니다.",
            example = "2026-10-15T11:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime startAt,
    @Schema(
            description = "코스 종료 일시입니다.",
            example = "2026-10-15T15:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime endAt,
    @Schema(description = "코스 조회수입니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        int viewCount) {

  /**
   * 코스 조회 결과를 API 응답으로 변환합니다.
   *
   * @param result 코스 조회 결과
   * @return 코스 API 응답
   */
  public static CourseResponse from(CourseResult result) {
    return new CourseResponse(
        result.courseId(),
        result.title(),
        result.courseSummary(),
        result.transport(),
        result.startAt(),
        result.endAt(),
        result.viewCount());
  }
}

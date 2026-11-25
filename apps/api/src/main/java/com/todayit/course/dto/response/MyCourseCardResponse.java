package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.model.MyCourseCardResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 코스 조회 목록의 코스 카드 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param title 코스 제목
 * @param status 코스 상태
 * @param visibility 코스 공개 범위
 * @param thumbnailUrl 코스 대표 썸네일 이미지 URL
 * @param placeCount 포함된 장소 수
 * @param sidoName 시·도 이름
 * @param sigunguName 시·군·구 이름
 * @param themes 테마/콘셉트 목록
 * @param likeCount 좋아요 수
 * @param startAt 코스 시작 일시
 * @param endAt 코스 종료 일시
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 */
@Schema(description = "내 코스 카드 응답")
public record MyCourseCardResponse(
    @Schema(description = "코스 식별자", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "코스 제목 (임시저장 코스는 미입력 시 null)",
            example = "데이트 코스",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
    @Schema(description = "코스 상태", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        CourseStatus status,
    @Schema(
            description = "코스 공개 범위",
            example = "PUBLIC",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseVisibility visibility,
    @Schema(
            description = "대표 썸네일 이미지 URL",
            example = "https://example.com/place.jpg",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String thumbnailUrl,
    @Schema(description = "포함된 장소 수", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        int placeCount,
    @Schema(
            description = "시·도 이름",
            example = "서울특별시",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sidoName,
    @Schema(
            description = "시·군·구 이름",
            example = "종로구",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String sigunguName,
    @Schema(
            description = "테마/콘셉트 목록",
            example = "[\"카페·디저트\", \"데이트\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> themes,
    @Schema(description = "좋아요 수", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        long likeCount,
    @Schema(
            description = "코스 시작 일시",
            example = "2026-11-24T10:00:00",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime startAt,
    @Schema(
            description = "코스 종료 일시",
            example = "2026-11-24T18:00:00",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime endAt,
    @Schema(
            description = "생성 일시",
            example = "2026-11-24T09:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
    @Schema(
            description = "수정 일시",
            example = "2026-11-24T12:00:00",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt) {

  /**
   * MyCourseCardResult 도메인 모델을 MyCourseCardResponse DTO로 변환합니다.
   *
   * @param result 내 코스 카드 결과 모델
   * @return 내 코스 카드 응답 DTO
   */
  public static MyCourseCardResponse from(MyCourseCardResult result) {
    return new MyCourseCardResponse(
        result.courseId(),
        result.title(),
        result.status(),
        result.visibility(),
        result.thumbnailUrl(),
        result.placeCount(),
        result.sidoName(),
        result.sigunguName(),
        result.themes(),
        result.likeCount(),
        result.startAt(),
        result.endAt(),
        result.createdAt(),
        result.updatedAt());
  }
}

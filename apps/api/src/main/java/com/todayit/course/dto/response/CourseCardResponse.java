package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.model.CourseCardResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 목록 및 필터 조회의 코스 카드 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param title 저장된 코스 제목
 * @param courseSummary 저장된 소개글
 * @param thumbnailUrl 대표 이미지 URL, 없으면 null
 * @param region 코스 지역 요약, 없으면 null
 * @param themes 콘셉트 이름 목록
 * @param durationMinutes 예상 소요 시간(분, endAt - startAt), 미확인 시 null
 * @param transport CAR, WALK 또는 PUBLIC_TRANSIT, 미확인 시 null
 * @param status 코스 상태 (CONFIRMED)
 * @param visibility 공개 범위 (PUBLIC)
 * @param placeCount 방문 장소 수
 * @param likeCount 좋아요 수
 * @param createdAt 생성 시각
 * @param updatedAt 최근 공개·수정 시각, 없으면 null
 */
@Schema(description = "코스 목록 카드 응답")
public record CourseCardResponse(
    @Schema(description = "코스 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "코스 제목입니다.",
            example = "행궁동 로맨틱 산책 코스",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
    @Schema(
            description = "코스 소개글입니다.",
            example = "행궁동 일대를 여유롭게 산책하는 데이트 코스입니다.",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String courseSummary,
    @Schema(
            description = "대표 썸네일 이미지 URL입니다. 이미지가 없으면 null입니다.",
            example = "https://example.com/place.jpg",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String thumbnailUrl,
    @Schema(
            description = "코스 지역 요약 정보입니다.",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseConditionRegionResponse region,
    @Schema(
            description = "코스 콘셉트 목록입니다.",
            example = "[\"로맨틱\", \"아늑함\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> themes,
    @Schema(
            description = "예상 소요 시간(분, endAt - startAt)입니다. 시작·종료 시각이 없으면 null입니다.",
            example = "180",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        Integer durationMinutes,
    @Schema(
            description = "이동수단입니다. (CAR, WALK, PUBLIC_TRANSIT)",
            example = "WALK",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String transport,
    @Schema(
            description = "코스 상태입니다.",
            example = "CONFIRMED",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String status,
    @Schema(
            description = "코스 공개 범위입니다.",
            example = "PUBLIC",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseVisibility visibility,
    @Schema(
            description = "포함된 방문 장소 수입니다.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int placeCount,
    @Schema(description = "좋아요 수입니다.", example = "128", requiredMode = Schema.RequiredMode.REQUIRED)
        long likeCount,
    @Schema(
            description = "생성 일시입니다.",
            example = "2026-10-12T10:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
    @Schema(
            description = "최근 수정·공개 일시입니다.",
            example = "2026-10-12T12:00:00",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt) {

  /** 콘셉트 목록을 방어적으로 복사합니다. */
  public CourseCardResponse {
    themes = themes == null ? List.of() : List.copyOf(themes);
  }

  /**
   * 코스 카드 조회 결과를 응답 DTO로 변환합니다.
   *
   * @param result 코스 카드 결과 모델
   * @return 코스 카드 응답 DTO
   */
  public static CourseCardResponse from(CourseCardResult result) {
    String transport =
        result.transport() == null
            ? null
            : switch (result.transport()) {
              case CAR -> "CAR";
              case WALKING -> "WALK";
              case PUBLIC_TRANSPORT -> "PUBLIC_TRANSIT";
            };
    return new CourseCardResponse(
        result.courseId(),
        result.title(),
        result.courseSummary(),
        result.thumbnailUrl(),
        CourseConditionRegionResponse.from(result.region()),
        result.themes(),
        result.durationMinutes(),
        transport,
        "CONFIRMED",
        result.visibility(),
        result.placeCount(),
        result.likeCount(),
        result.createdAt(),
        result.updatedAt());
  }
}

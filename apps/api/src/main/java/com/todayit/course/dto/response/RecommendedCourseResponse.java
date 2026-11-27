package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.model.RecommendedCourseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 공개 확정 코스의 추천 카드 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param title 저장된 코스 제목
 * @param courseSummary 저장된 소개글
 * @param thumbnailUrl 대표 이미지, 없으면 null
 * @param region 코스 지역 요약, 없으면 null
 * @param themes 콘셉트 이름 목록
 * @param transport CAR, WALK 또는 PUBLIC_TRANSIT, 미확인 시 null
 * @param status 코스 상태
 * @param visibility 공개 범위
 * @param placeCount 방문 수
 * @param likeCount 좋아요 수
 * @param updatedAt 최근 공개 시각, 미확인 시 null
 */
@Schema(description = "추천 코스 카드 응답")
public record RecommendedCourseResponse(
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
            description = "최근 수정·공개 일시입니다.",
            example = "2026-10-12T12:00:00",
            nullable = true,
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt) {
  /** 콘셉트 목록을 방어적으로 복사합니다. */
  public RecommendedCourseResponse {
    themes = List.copyOf(themes);
  }

  /**
   * 추천 카드 조회 결과를 응답으로 변환합니다.
   *
   * @param result 추천 카드 결과
   * @return 추천 카드 응답
   */
  public static RecommendedCourseResponse from(RecommendedCourseResult result) {
    String transport =
        result.transport() == null
            ? null
            : switch (result.transport()) {
              case CAR -> "CAR";
              case WALKING -> "WALK";
              case PUBLIC_TRANSPORT -> "PUBLIC_TRANSIT";
            };
    return new RecommendedCourseResponse(
        result.courseId(),
        result.title(),
        result.courseSummary(),
        result.thumbnailUrl(),
        CourseConditionRegionResponse.from(result.region()),
        result.themes(),
        transport,
        "CONFIRMED",
        CourseVisibility.PUBLIC,
        result.placeCount(),
        result.likeCount(),
        result.updatedAt());
  }
}

package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.model.RecommendedCourseResult;
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
public record RecommendedCourseResponse(
    int courseId,
    String title,
    String courseSummary,
    String thumbnailUrl,
    CourseConditionRegionResponse region,
    List<String> themes,
    String transport,
    String status,
    CourseVisibility visibility,
    int placeCount,
    long likeCount,
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

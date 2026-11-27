package com.todayit.course.dto.response;

import com.todayit.course.service.model.RecommendedCourseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 추천 코스 목록 응답입니다.
 *
 * @param courses 추천 순서의 코스 카드 목록
 */
@Schema(description = "회원별 추천 코스 목록 응답")
public record RecommendedCoursesResponse(
    @Schema(description = "추천 순서의 코스 카드 목록입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        List<RecommendedCourseResponse> courses) {
  /** 추천 코스 목록을 방어적으로 복사합니다. */
  public RecommendedCoursesResponse {
    courses = List.copyOf(courses);
  }

  /**
   * 추천 카드 결과를 HTTP 응답으로 변환합니다.
   *
   * @param results 추천 순서의 조회 결과
   * @return 추천 목록 응답
   */
  public static RecommendedCoursesResponse from(List<RecommendedCourseResult> results) {
    return new RecommendedCoursesResponse(
        results.stream().map(RecommendedCourseResponse::from).toList());
  }
}

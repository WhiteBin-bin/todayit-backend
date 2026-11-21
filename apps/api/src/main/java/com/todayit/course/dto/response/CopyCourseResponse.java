package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseStatus;
import com.todayit.course.service.model.CopyCourseResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 복사 응답 DTO입니다.
 *
 * @param courseId 새로 생성된 DRAFT 코스 식별자
 * @param originalCourseId 원본 코스 식별자
 * @param title 코스명 (복사 직후 null)
 * @param status 코스 상태 (DRAFT)
 * @param places 복사된 장소 목록 (원본 순서 유지, 체류시간 null)
 * @param createdAt 생성 일시
 */
@Schema(description = "코스 복사 결과 응답")
public record CopyCourseResponse(
    @Schema(description = "새로 생성된 DRAFT 코스 식별자", example = "4") int courseId,
    @Schema(description = "복사 대상이 된 원본 코스 식별자", example = "1") int originalCourseId,
    @Schema(description = "코스명 (복사 직후 null)", example = "null", nullable = true) String title,
    @Schema(description = "코스 상태", example = "DRAFT") CourseStatus status,
    @Schema(description = "복사된 장소 목록 (원본 순서 유지, 체류시간 null)") List<CoursePlaceResponse> places,
    @Schema(description = "생성 일시", example = "2026-11-19T17:32:45") LocalDateTime createdAt) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param courseId 새로 생성된 DRAFT 코스 식별자
   * @param originalCourseId 원본 코스 식별자
   * @param title 코스명
   * @param status 코스 상태
   * @param places 복사된 장소 목록
   * @param createdAt 생성 일시
   */
  public CopyCourseResponse {
    places = places == null ? List.of() : List.copyOf(places);
  }

  /**
   * 서비스 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 복사 결과 모델
   * @return 코스 복사 응답 DTO
   */
  public static CopyCourseResponse from(CopyCourseResult result) {
    return new CopyCourseResponse(
        result.courseId(),
        result.originalCourseId(),
        result.title(),
        result.status(),
        result.places().stream().map(CoursePlaceResponse::from).toList(),
        result.createdAt());
  }
}

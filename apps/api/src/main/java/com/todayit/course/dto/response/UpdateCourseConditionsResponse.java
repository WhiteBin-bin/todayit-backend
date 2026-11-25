package com.todayit.course.dto.response;

import com.todayit.course.service.model.UpdateCourseConditionsResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 생성 조건 수정 응답 DTO입니다.
 *
 * @param courseId 코스 식별자
 * @param conditions 수정된 생성 조건 정보
 * @param places 코스에 포함된 장소 목록
 * @param updatedAt 수정 일시
 */
@Schema(description = "코스 생성 조건 수정 응답")
public record UpdateCourseConditionsResponse(
    @Schema(description = "코스 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(description = "수정된 코스 생성 조건입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        CourseConditionsResponse conditions,
    @Schema(
            description = "코스에 포함된 장소 목록입니다. 순서 오름차순으로 정렬됩니다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<CoursePlaceResponse> places,
    @Schema(
            description = "코스 수정 일시입니다.",
            example = "2026-11-24T18:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt) {

  /**
   * 장소 목록을 방어적으로 복사하여 초기화합니다.
   *
   * @param courseId 코스 식별자
   * @param conditions 수정된 생성 조건 정보
   * @param places 코스에 포함된 장소 목록
   * @param updatedAt 수정 일시
   */
  public UpdateCourseConditionsResponse {
    places = places != null ? List.copyOf(places) : List.of();
  }

  /**
   * 서비스 계층의 수정 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 서비스 수정 결과 모델
   * @return 코스 생성 조건 수정 응답 DTO
   */
  public static UpdateCourseConditionsResponse from(UpdateCourseConditionsResult result) {
    List<CoursePlaceResponse> placeResponses =
        result.places().stream().map(CoursePlaceResponse::from).toList();
    return new UpdateCourseConditionsResponse(
        result.courseId(),
        CourseConditionsResponse.from(result.conditions()),
        placeResponses,
        result.updatedAt());
  }
}

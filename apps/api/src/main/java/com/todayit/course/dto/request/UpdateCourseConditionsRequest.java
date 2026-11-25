package com.todayit.course.dto.request;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.service.command.UpdateCourseConditionsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 코스 생성 조건 수정 요청 DTO입니다.
 *
 * @param region 변경할 지역 코드 (시도 2자리 또는 시군구 3자리)
 * @param startAt 방문 시작 일시
 * @param endAt 방문 종료 일시
 * @param durationMinutes 소요 시간(분)
 * @param transport 이동수단
 */
@Schema(description = "임시저장 코스의 생성 조건을 수정하기 위한 요청 정보입니다. 모든 필드는 선택 사항이며, 입력된 값만 수정됩니다.")
public record UpdateCourseConditionsRequest(
    @Schema(
            description = "변경할 행정 지역 코드입니다. 시·도 2자리, 시·도+시·군·구 5자리, 또는 유일한 시·군·구 3자리로 입력합니다.",
            example = "11",
            nullable = true)
        String region,
    @Schema(
            description = "코스 방문 시작 일시입니다. 2026-12-01T10:00:00 형식으로 입력합니다.",
            example = "2026-12-01T10:00:00",
            nullable = true)
        LocalDateTime startAt,
    @Schema(
            description = "코스 방문 종료 일시입니다. 시작 시각 이후이어야 하며, 2026-12-01T14:00:00 형식으로 입력합니다.",
            example = "2026-12-01T14:00:00",
            nullable = true)
        LocalDateTime endAt,
    @Schema(description = "예상 소요 시간(분)입니다. 1 이상의 양수이어야 합니다.", example = "240", nullable = true)
        Integer durationMinutes,
    @Schema(
            description = "코스 이동수단입니다. CAR, WALKING, PUBLIC_TRANSPORT 중 하나를 입력합니다.",
            example = "CAR",
            nullable = true)
        CourseTransport transport) {

  /**
   * 요청 값의 형식 및 유효성을 검증합니다.
   *
   * @throws CourseException 지역 코드 형식 오류, 일시 순서 오류, 또는 소요 시간 오류 시
   */
  public void validate() {
    if (region != null && !region.isBlank()) {
      String strippedRegion = region.strip();
      if (!strippedRegion.matches("^[0-9]{2}$")
          && !strippedRegion.matches("^[0-9]{3}$")
          && !strippedRegion.matches("^[0-9]{5}$")) {
        throw new CourseException(CourseErrorCode.INVALID_REGION);
      }
    }
    if (durationMinutes == null && startAt != null && endAt != null && !endAt.isAfter(startAt)) {
      throw new CourseException(CourseErrorCode.INVALID_END_AT);
    }
    if (durationMinutes != null && durationMinutes <= 0) {
      throw new CourseException(CourseErrorCode.INVALID_END_AT);
    }
  }

  /**
   * 서비스 명령 객체로 변환합니다.
   *
   * @param courseId 코스 식별자
   * @return 코스 생성 조건 수정 명령
   */
  public UpdateCourseConditionsCommand toCommand(int courseId) {
    return new UpdateCourseConditionsCommand(
        courseId,
        region != null && !region.isBlank() ? region.strip() : null,
        startAt,
        endAt,
        durationMinutes,
        transport);
  }
}

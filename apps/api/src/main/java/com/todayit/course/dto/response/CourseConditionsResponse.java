package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.model.CourseConditionsResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 코스 생성 조건 응답 DTO입니다.
 *
 * @param region 행정 지역 정보, 없으면 null
 * @param startAt 방문 시작 일시
 * @param endAt 방문 종료 일시, 없으면 null
 * @param durationMinutes 총 소요 시간(분), 없으면 null
 * @param transport 이동 수단
 */
@Schema(description = "코스 생성 조건 응답")
public record CourseConditionsResponse(
    @Schema(description = "행정 지역 정보입니다. 설정된 지역이 없으면 null입니다.", nullable = true)
        CourseConditionRegionResponse region,
    @Schema(description = "코스 시작 일시입니다.", example = "2026-12-01T10:00:00") LocalDateTime startAt,
    @Schema(description = "코스 종료 일시입니다.", example = "2026-12-01T14:00:00", nullable = true)
        LocalDateTime endAt,
    @Schema(description = "총 소요 시간(분)입니다.", example = "240", nullable = true)
        Integer durationMinutes,
    @Schema(description = "이동 수단입니다.", example = "CAR") CourseTransport transport) {

  /**
   * 서비스 계층의 조건 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 서비스 조건 결과 모델
   * @return 코스 생성 조건 응답 DTO, 전달된 결과가 null이면 null
   */
  public static CourseConditionsResponse from(CourseConditionsResult result) {
    if (result == null) {
      return null;
    }
    return new CourseConditionsResponse(
        CourseConditionRegionResponse.from(result.region()),
        result.startAt(),
        result.endAt(),
        result.durationMinutes(),
        result.transport());
  }
}

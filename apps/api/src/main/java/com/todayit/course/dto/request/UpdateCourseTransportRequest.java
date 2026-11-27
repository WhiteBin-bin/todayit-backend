package com.todayit.course.dto.request;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 이동수단 변경 요청 DTO입니다.
 *
 * @param transport 변경할 이동수단
 */
@Schema(description = "임시저장 코스의 이동수단 변경 요청 정보입니다.")
public record UpdateCourseTransportRequest(
    @Schema(
            description =
                """
                변경할 코스 이동수단입니다.

                - `CAR`: 자동차
                - `WALKING`: 도보
                - `PUBLIC_TRANSPORT`: 대중교통
                """,
            example = "CAR",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseTransport transport) {

  /**
   * 이동수단 필수 입력값을 검증합니다.
   *
   * @throws CourseException 이동수단이 누락된 경우
   */
  public void validate() {
    if (transport == null) {
      throw new CourseException(CourseErrorCode.TRANSPORT_REQUIRED);
    }
  }
}

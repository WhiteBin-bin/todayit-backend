package com.todayit.course.dto.request;

import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 장소 추가 요청입니다.
 *
 * @param placeId 장소 식별자
 * @param order 방문 순서, 미입력 시 맨 뒤로 자동 배치
 * @param stayMinute 머무는 시간(분), 미입력 시 0
 */
@Schema(description = "코스에 추가할 장소 정보입니다.")
public record AddCoursePlaceRequest(
    @Schema(
            description =
                """
                추가할 장소 식별자입니다.

                - 1 이상의 정수로 입력합니다.
                """,
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Integer placeId,
    @Schema(
            description =
                """
                방문 순서입니다.

                - 1 이상의 정수로 입력합니다.
                - 생략하거나 `null`이면 코스의 마지막 순서 다음으로 자동 지정됩니다.
                """,
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true)
        Integer order,
    @Schema(
            description =
                """
                체류 시간(분)입니다.

                - 0분 이상 1440분(24시간) 이하로 입력합니다.
                - 생략하거나 `null`이면 `0`으로 지정됩니다.
                """,
            example = "60",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true)
        Integer stayMinute) {

  private static final int MAX_STAY_MINUTE = 1440;

  /**
   * 입력값을 검증합니다.
   *
   * @throws CourseException 입력 조건이 올바르지 않은 경우
   */
  public void validate() {
    if (placeId == null || placeId < 1) {
      throw new CourseException(CourseErrorCode.INVALID_PLACE_ID);
    }
    if (order != null && order < 1) {
      throw new CourseException(CourseErrorCode.INVALID_ORDER);
    }
    if (stayMinute != null && (stayMinute < 0 || stayMinute > MAX_STAY_MINUTE)) {
      throw new CourseException(CourseErrorCode.INVALID_STAY_MINUTE);
    }
  }

  /**
   * 기본값이 적용된 체류 시간을 반환합니다.
   *
   * @return 0 이상의 체류 시간
   */
  public int resolveStayMinute() {
    return stayMinute != null ? stayMinute : 0;
  }
}

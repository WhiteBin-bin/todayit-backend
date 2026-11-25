package com.todayit.course.dto.request;

import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 코스 장소 순서 변경 요청입니다.
 *
 * @param coursePlaceIds 변경할 방문 순서대로 정렬된 코스 장소 식별자 목록
 */
@Schema(description = "코스 장소 순서 변경 요청")
public record UpdateCoursePlaceOrdersRequest(
    @Schema(
            description = "새로운 방문 순서대로 나열된 코스 장소 식별자 목록입니다. 코스의 모든 장소 식별자를 중복 없이 포함해야 합니다.",
            example = "[3, 1, 2]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "코스 장소 식별자 목록은 필수입니다.")
        @NotEmpty(message = "코스 장소 식별자 목록은 비어 있을 수 없습니다.")
        List<Integer> coursePlaceIds) {

  /**
   * 컴팩트 생성자에서 방어적 복사를 수행합니다.
   *
   * @param coursePlaceIds 코스 장소 식별자 목록
   */
  public UpdateCoursePlaceOrdersRequest {
    coursePlaceIds = coursePlaceIds == null ? List.of() : List.copyOf(coursePlaceIds);
  }

  /**
   * 요청값의 유효성을 검증합니다.
   *
   * @throws CourseException 식별자 목록이 비어 있거나 음수 또는 중복이 있는 경우
   */
  public void validate() {
    if (coursePlaceIds == null || coursePlaceIds.isEmpty()) {
      throw new CourseException(CourseErrorCode.INVALID_ORDER);
    }
    for (Integer id : coursePlaceIds) {
      if (id == null || id <= 0) {
        throw new CourseException(CourseErrorCode.INVALID_ORDER);
      }
    }
    if (coursePlaceIds.stream().distinct().count() != coursePlaceIds.size()) {
      throw new CourseException(CourseErrorCode.INVALID_ORDER);
    }
  }
}

package com.todayit.course.dto.response;

import com.todayit.course.service.model.UpdateCoursePlaceOrdersResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 코스 장소 순서 변경 응답 DTO입니다.
 *
 * @param places 순서 변경 후 정렬된 전체 코스 장소 목록
 */
@Schema(description = "코스 장소 순서 변경 결과 응답")
public record UpdateCoursePlaceOrdersResponse(
    @Schema(description = "순서 변경 후 정렬된 전체 코스 장소 목록") List<CoursePlaceResponse> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param places 전체 코스 장소 목록
   */
  public UpdateCoursePlaceOrdersResponse {
    places = places == null ? List.of() : List.copyOf(places);
  }

  /**
   * 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 장소 순서 변경 결과 모델
   * @return 코스 장소 순서 변경 응답 DTO
   */
  public static UpdateCoursePlaceOrdersResponse from(UpdateCoursePlaceOrdersResult result) {
    return new UpdateCoursePlaceOrdersResponse(
        result.places().stream().map(CoursePlaceResponse::from).toList());
  }
}

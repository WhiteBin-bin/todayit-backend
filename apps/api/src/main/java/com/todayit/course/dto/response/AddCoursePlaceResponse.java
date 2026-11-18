package com.todayit.course.dto.response;

import com.todayit.course.service.model.AddCoursePlaceResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코스 장소 추가 응답입니다.
 *
 * @param coursePlaceId 생성된 코스 장소 식별자
 * @param placeId 장소 식별자
 * @param order 방문 순서
 * @param stayMinute 체류 시간(분)
 */
@Schema(description = "코스 장소 추가 결과입니다.")
public record AddCoursePlaceResponse(
    @Schema(description = "코스 장소 관계 식별자", example = "1") int coursePlaceId,
    @Schema(description = "장소 식별자", example = "10") int placeId,
    @Schema(description = "방문 순서", example = "1") int order,
    @Schema(description = "체류 시간(분)", example = "60") int stayMinute) {

  /**
   * 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 장소 추가 결과 모델
   * @return 코스 장소 추가 응답 DTO
   */
  public static AddCoursePlaceResponse from(AddCoursePlaceResult result) {
    return new AddCoursePlaceResponse(
        result.coursePlaceId(), result.placeId(), result.order(), result.stayMinute());
  }
}

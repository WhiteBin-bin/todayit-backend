package com.todayit.course.dto.response;

import com.todayit.course.service.model.AddCoursePlaceResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 코스 장소 추가 응답 DTO입니다.
 *
 * @param addedPlace 추가된 코스 장소 상세 정보
 * @param places 장소 추가 후 최신 정렬된 전체 코스 장소 목록
 */
@Schema(description = "코스 장소 추가 결과 응답")
public record AddCoursePlaceResponse(
    @Schema(description = "추가된 코스 장소 상세 정보") CoursePlaceResponse addedPlace,
    @Schema(description = "장소 추가 후 최신 정렬된 전체 코스 장소 목록") List<CoursePlaceResponse> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param addedPlace 추가된 코스 장소 정보
   * @param places 전체 코스 장소 목록
   */
  public AddCoursePlaceResponse {
    places = places == null ? List.of() : List.copyOf(places);
  }

  /**
   * 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 장소 추가 결과 모델
   * @return 코스 장소 추가 응답 DTO
   */
  public static AddCoursePlaceResponse from(AddCoursePlaceResult result) {
    return new AddCoursePlaceResponse(
        CoursePlaceResponse.from(result.addedPlace()),
        result.places().stream().map(CoursePlaceResponse::from).toList());
  }
}

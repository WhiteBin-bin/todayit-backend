package com.todayit.course.dto.response;

import com.todayit.course.service.model.RemoveCoursePlaceResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 코스 장소 삭제 응답 DTO입니다.
 *
 * @param deletedCoursePlaceId 삭제된 코스 장소 식별자
 * @param places 삭제 후 재정렬된 남은 코스 장소 목록
 */
@Schema(description = "코스 장소 삭제 및 순서 재조정 결과 응답")
public record RemoveCoursePlaceResponse(
    @Schema(description = "삭제된 코스 장소 식별자", example = "102") int deletedCoursePlaceId,
    @Schema(description = "삭제 후 재정렬된 남은 코스 장소 목록") List<CoursePlaceResponse> places) {

  /**
   * 장소 목록을 방어적으로 복사합니다.
   *
   * @param deletedCoursePlaceId 삭제된 코스 장소 식별자
   * @param places 남은 장소 목록
   */
  public RemoveCoursePlaceResponse {
    places = places == null ? List.of() : List.copyOf(places);
  }

  /**
   * 서비스 결과 모델로부터 응답 DTO를 생성합니다.
   *
   * @param result 코스 장소 삭제 결과 모델
   * @return 코스 장소 삭제 응답 DTO
   */
  public static RemoveCoursePlaceResponse from(RemoveCoursePlaceResult result) {
    return new RemoveCoursePlaceResponse(
        result.deletedCoursePlaceId(),
        result.places().stream().map(CoursePlaceResponse::from).toList());
  }
}

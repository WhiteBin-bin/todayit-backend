package com.todayit.course.dto.response;

import com.todayit.course.service.model.CoursePlaceDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * 코스 상세 응답에 포함되는 장소 정보입니다.
 *
 * @param coursePlaceId 코스 장소 식별자
 * @param placeId 장소 식별자
 * @param name 장소 이름
 * @param category 장소 카테고리
 * @param address 장소 주소
 * @param latitude 장소 위도
 * @param longitude 장소 경도
 * @param imageUrl 장소 대표 이미지 URL, 없으면 null
 * @param order 방문 순서 (1부터 시작)
 * @param stayMinute 예상 체류 시간(분), 미입력 시 null
 */
@Schema(description = "코스에 포함된 장소 정보")
public record CoursePlaceResponse(
    @Schema(
            description = "코스 장소 식별자입니다.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int coursePlaceId,
    @Schema(description = "장소 식별자입니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        int placeId,
    @Schema(
            description = "장소 이름입니다.",
            example = "블루보틀 삼청",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
    @Schema(
            description = "장소 카테고리입니다.",
            example = "CAFE_DESSERT",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String category,
    @Schema(
            description = "장소 주소입니다.",
            example = "서울특별시 종로구 북촌로 5길",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String address,
    @Schema(
            description = "장소의 위도입니다.",
            example = "37.58123400",
            requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal latitude,
    @Schema(
            description = "장소의 경도입니다.",
            example = "126.98123400",
            requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal longitude,
    @Schema(
            description = "장소 대표 이미지 URL입니다. 등록된 이미지가 없으면 null을 반환합니다.",
            example = "https://example.com/images/place-1.jpg",
            nullable = true)
        String imageUrl,
    @Schema(
            description = "방문 순서입니다. 1부터 시작합니다.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        int order,
    @Schema(description = "예상 체류 시간(분)입니다. 미입력 시 null을 반환합니다.", example = "60", nullable = true)
        Integer stayMinute) {

  /**
   * 서비스 장소 상세 결과를 API 응답 모델로 변환합니다.
   *
   * @param result 서비스 장소 상세 결과
   * @return 코스 장소 응답
   */
  public static CoursePlaceResponse from(CoursePlaceDetailResult result) {
    return new CoursePlaceResponse(
        result.coursePlaceId(),
        result.placeId(),
        result.name(),
        result.category(),
        result.address(),
        result.latitude(),
        result.longitude(),
        result.imageUrl(),
        result.order(),
        result.stayMinute());
  }
}

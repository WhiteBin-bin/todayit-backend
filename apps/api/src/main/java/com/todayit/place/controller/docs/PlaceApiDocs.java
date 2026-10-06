package com.todayit.place.controller.docs;

import com.todayit.common.openapi.OpenApiConfig;
import com.todayit.common.pagination.PageResponse;
import com.todayit.common.response.CommonResponse;
import com.todayit.common.response.ErrorResponse;
import com.todayit.course.dto.response.CourseResponse;
import com.todayit.course.service.model.CourseSort;
import com.todayit.place.dto.response.PlaceImageResponse;
import com.todayit.place.dto.response.PlaceLikeResponse;
import com.todayit.place.dto.response.PlaceLocationResponse;
import com.todayit.place.dto.response.PlaceResponse;
import com.todayit.place.dto.response.PlaceScrapResponse;
import com.todayit.place.dto.response.PlaceShareResponse;
import com.todayit.place.service.model.PlaceSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/** 장소 조회와 스크랩 API 명세입니다. */
@Tag(name = "장소", description = "장소 조회와 스크랩 API")
public interface PlaceApiDocs {

  /**
   * 장소 목록을 페이지 단위로 조회합니다.
   *
   * @param region 단일 시·도·시·군·구 이름 또는 상위 지역부터 입력한 경로
   * @param categories 장소 카테고리 목록
   * @param weekdays 영업 요일
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @param sort 정렬 기준
   * @return 장소 목록과 페이지 정보
   */
  @Operation(
      summary = "장소 필터 조회",
      description =
          "지역, 카테고리, 영업 요일을 선택해 활성 장소 목록을 페이지 단위로 조회합니다. "
              + "시·도 또는 시·군을 선택하면 모든 하위 지역을 포함하며 구만 단독으로 선택할 수도 있습니다. "
              + "상위 지역과 하위 지역을 함께 전달하면 해당 경로만 조회합니다. "
              + "예: 서울특별시, 수원시, 중구, 서울특별시 동대문구, 수원시 팔달구")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"content\":[{\"placeId\":1,\"name\":\"오늘의 식당\",\"latitude\":37.57000000,\"longitude\":126.98500000,\"address\":\"서울특별시 종로구 종로 1\",\"category\":\"RESTAURANT\",\"viewCount\":15,\"imageUrls\":[\"https://example.com/place.jpg\"]}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 필터 또는 페이지 요청",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "잘못된 카테고리",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_PLACE_CATEGORY\",\"message\":\"카테고리 필터 값이 올바르지 않습니다.\"}"),
                  @ExampleObject(
                      name = "잘못된 요일",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_PLACE_WEEKDAY\",\"message\":\"요일 필터 값이 올바르지 않습니다.\"}"),
                  @ExampleObject(
                      name = "잘못된 지역 경로",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REGION_PATH\",\"message\":\"지역 필터 값이 올바르지 않습니다.\"}"),
                  @ExampleObject(
                      name = "잘못된 페이지",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_PAGINATION\",\"message\":\"페이지 요청값이 올바르지 않습니다.\"} shipment")
                })),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PageResponse<PlaceResponse>>> findPlaces(
      String region, List<String> categories, String weekdays, int page, int size, PlaceSort sort);

  /**
   * 장소의 지도 표시 정보를 조회합니다.
   *
   * @param placeId 장소 식별자
   * @return 장소 위치 정보
   */
  @Operation(summary = "장소 지도 조회", description = "장소의 위도, 경도와 주소를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"placeId\":1,\"latitude\":37.57000000,\"longitude\":126.98500000,\"address\":\"서울특별시 종로구 종로 1\"}}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PlaceLocationResponse>> findPlaceLocation(int placeId);

  /**
   * 장소의 사진 목록을 페이지 단위로 조회합니다.
   *
   * @param placeId 장소 식별자
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @return 장소 사진 목록과 페이지 정보
   */
  @Operation(summary = "장소 사진 조회", description = "장소의 사진 목록을 페이지 단위로 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"content\":[{\"placeImageId\":10,\"imageUrl\":\"https://example.com/place-image.jpg\"}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 페이지 요청",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INVALID_PAGINATION\",\"message\":\"페이지 요청값이 올바르지 않습니다.\"}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PageResponse<PlaceImageResponse>>> findPlaceImages(
      int placeId, int page, int size);

  /**
   * 인증된 회원의 장소 스크랩을 생성합니다.
   *
   * @param placeId 장소 식별자
   * @param authentication 인증된 회원 정보
   * @return 장소 스크랩 결과
   */
  @Operation(
      summary = "장소 스크랩",
      description = "인증된 회원의 장소 스크랩을 생성합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "스크랩 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"placeId\":1,\"scrapped\":true,\"scrapCount\":3}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "이미 스크랩한 장소입니다",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_ALREADY_SCRAPPED\",\"message\":\"이미 스크랩되었습니다.\"}"))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"인증이 필요합니다.\"}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PlaceScrapResponse>> scrapPlace(
      int placeId, Authentication authentication);

  /**
   * 인증된 회원의 장소 스크랩을 취소합니다.
   *
   * @param placeId 장소 식별자
   * @param authentication 인증된 회원 정보
   * @return 장소 스크랩 취소 결과
   */
  @Operation(
      summary = "장소 스크랩 취소",
      description = "인증된 회원의 장소 스크랩을 취소합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "스크랩 취소 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"placeId\":1,\"scrapped\":false,\"scrapCount\":2}}"))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"인증이 필요합니다.\"}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PlaceScrapResponse>> cancelPlaceScrap(
      int placeId, Authentication authentication);

  /**
   * 특정 장소의 공개 코스 목록을 조회합니다.
   *
   * @param placeId 장소 식별자
   * @param page 페이지 번호
   * @param size 페이지 크기
   * @param sort 코스 정렬 기준
   * @return 장소를 포함한 공개 코스 목록과 페이지 정보
   */
  @Operation(summary = "장소별 코스 조회", description = "특정 장소를 포함한 공개 코스 목록을 페이지 단위로 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"content\":[{\"courseId\":1,\"title\":\"종로 맛집과 카페 코스\",\"courseSummary\":\"종로에서 식사와 카페를 함께 즐기는 코스입니다.\",\"transport\":\"WALKING\",\"startAt\":\"2026-10-03T11:00:00\",\"endAt\":\"2026-10-03T15:00:00\",\"viewCount\":25}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 페이지 요청",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INVALID_PAGINATION\",\"message\":\"페이지 요청값이 올바르지 않습니다.\"}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PageResponse<CourseResponse>>> findCoursesByPlace(
      int placeId, int page, int size, CourseSort sort);

  /**
   * 인증된 회원의 장소 좋아요를 생성합니다.
   *
   * @param placeId 장소 식별자
   * @param authentication 인증된 회원 정보
   * @return 장소 좋아요 결과
   */
  @Operation(
      summary = "장소 좋아요",
      description = "인증된 회원의 장소 좋아요를 생성합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "좋아요 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"placeId\":1,\"liked\":true,\"likeCount\":3}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "이미 좋아요를 누른 장소입니다",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_ALREADY_LIKED\",\"message\":\"이미 좋아요를 눌렀습니다.\"}"))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 필요",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"인증이 필요합니다.\"}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PlaceLikeResponse>> likePlace(
      int placeId, Authentication authentication);

  /**
   * 장소 공유 링크를 생성합니다.
   *
   * @param placeId 장소 식별자
   * @return 장소 공유 링크 정보
   */
  @Operation(
      summary = "장소 공유하기",
      description = "장소 식별자를 기반으로 공유 가능한 URL과 만료 일시를 반환합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "공유 링크 생성 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"placeId\":1,\"shareUrl\":\"http://localhost:3000/places/1\",\"expiresAt\":\"2026-10-13T14:35:57\"}}"))),
    @ApiResponse(
        responseCode = "404",
        description = "장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"PLACE_NOT_FOUND\",\"message\":\"장소를 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<PlaceShareResponse>> sharePlace(int placeId);
}

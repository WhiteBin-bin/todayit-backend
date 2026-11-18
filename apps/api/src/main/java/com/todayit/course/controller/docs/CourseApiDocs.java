package com.todayit.course.controller.docs;

import com.todayit.common.openapi.OpenApiConfig;
import com.todayit.common.response.CommonResponse;
import com.todayit.common.response.ErrorResponse;
import com.todayit.course.dto.request.AddCoursePlaceRequest;
import com.todayit.course.dto.request.CreateCourseRequest;
import com.todayit.course.dto.request.UpdateCoursePlaceOrdersRequest;
import com.todayit.course.dto.response.AddCoursePlaceResponse;
import com.todayit.course.dto.response.CourseDetailResponse;
import com.todayit.course.dto.response.CreateCourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

/** 코스 생성, 조회 및 관리 API 문서입니다. */
@Tag(name = "코스", description = "코스 생성, 조회 및 관리 API")
public interface CourseApiDocs {

  /**
   * 새로운 코스를 DRAFT 상태로 생성합니다.
   *
   * @param request 코스 생성 요청 본문
   * @param authentication 인증된 회원 정보
   * @return 생성된 코스 식별자
   */
  @Operation(
      summary = "새 코스 생성",
      description =
          "새로운 코스를 DRAFT 상태의 비공개 코스로 생성합니다. "
              + "로그인한 회원만 생성할 수 있습니다. "
              + "제목은 1–100자, 이동수단은 필수(WALKING, PUBLIC_TRANSPORT, CAR), "
              + "시작 시각(startAt)은 필수(ISO 8601), 종료 시각(endAt)은 선택이며 시작 시각 이후이어야 합니다. "
              + "콘셉트(themes)는 중복 없이 1–2개 필수 선택입니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 생성 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                    {
                      "success": true,
                      "data": {
                        "courseId": 1
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 유효성 오류 또는 지원하지 않는 콘셉트",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "코스명 누락 또는 길이 초과 (1~100자)",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_TITLE","message":"코스명은 1자 이상 100자 이하이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "콘셉트 개수 오류 (1~2개 필수, 중복 불가)",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_THEMES","message":"콘셉트는 중복 없이 1개 이상 2개 이하로 입력해야 합니다."}
                      """),
                  @ExampleObject(
                      name = "지원하지 않는 콘셉트 요청",
                      value =
                          """
                      {"success":false,"code":"UNSUPPORTED_COURSE_THEME","message":"지원하지 않는 콘셉트가 포함되어 있습니다."}
                      """),
                  @ExampleObject(
                      name = "시작 시각 누락",
                      value =
                          """
                      {"success":false,"code":"COURSE_START_AT_REQUIRED","message":"시작 시각은 필수입니다."}
                      """),
                  @ExampleObject(
                      name = "종료 시각이 시작 시각 이전이거나 동일",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_END_AT","message":"종료 시각은 시작 시각 이후이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "이동수단 누락",
                      value =
                          """
                      {"success":false,"code":"COURSE_TRANSPORT_REQUIRED","message":"이동수단은 필수입니다."}
                      """),
                  @ExampleObject(
                      name = "JSON·날짜 형식 또는 이동수단 enum 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_REQUEST","message":"요청값이 올바르지 않습니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 정보 누락, 유효하지 않은 토큰 또는 만료된 로그인 세션",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "인증 필요",
                        value =
                            """
                    {"success":false,"code":"AUTHENTICATION_REQUIRED","message":"인증이 필요합니다."}
                    """))),
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
  ResponseEntity<CommonResponse<CreateCourseResponse>> createCourse(
      CreateCourseRequest request, Authentication authentication);

  /**
   * 코스 식별자로 코스 상세 정보를 조회합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 선택 인증 정보
   * @return 코스 상세 정보
   */
  @Operation(
      summary = "코스 상세 조회",
      description =
          "코스 식별자로 코스 상세 정보를 조회합니다. "
              + "공개 코스는 누구나 조회할 수 있으며, 비공개 코스나 DRAFT 코스는 작성자 본인만 조회할 수 있습니다. "
              + "존재하지 않거나 삭제된 코스, 또는 권한이 없는 비공개 코스를 조회하면 404를 반환합니다. "
              + "비회원 또는 타인이 조회 시 isWriter는 false입니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 상세 조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                    {
                      "success": true,
                      "data": {
                        "courseId": 1,
                        "title": "종로 데이트 코스",
                        "courseSummary": "종로 감성 카페 투어",
                        "transport": "WALKING",
                        "startAt": "2026-12-01T10:00:00",
                        "endAt": "2026-12-01T18:00:00",
                        "status": "DRAFT",
                        "visibility": "PRIVATE",
                        "viewCount": 0,
                        "isWriter": true,
                        "sidoCode": "11",
                        "sidoName": "서울특별시",
                        "sigunguCode": "110",
                        "sigunguName": "종로구",
                        "themes": [
                          "감성적",
                          "여유로움"
                        ],
                        "places": [
                          {
                            "coursePlaceId": 1,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "CAFE_DESSERT",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/place.jpg",
                            "order": 1,
                            "stayMinute": 60
                          }
                        ],
                        "createdAt": "2026-11-17T12:00:00"
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스를 찾을 수 없거나 접근 권한이 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "코스 없음",
                        value =
                            """
                    {"success":false,"code":"COURSE_NOT_FOUND","message":"코스를 찾을 수 없습니다."}
                    """))),
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
  ResponseEntity<CommonResponse<CourseDetailResponse>> getCourseDetail(
      @Parameter(description = "조회할 코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스에 새 장소를 추가합니다.
   *
   * @param courseId 코스 식별자
   * @param request 추가할 장소 정보
   * @param authentication 인증된 작성자 정보
   * @return 추가된 코스 장소 정보
   */
  @Operation(
      summary = "코스 장소 추가",
      description =
          "작성 중인 코스에 새로운 장소를 추가합니다. 작성자 본인만 추가할 수 있습니다. "
              + "방문 순서(order)를 생략하면 현재 코스의 마지막 순서 다음으로 자동 지정되며, "
              + "체류 시간(stayMinute)을 생략하면 기본값 0분으로 저장됩니다. "
              + "동일한 장소(placeId)는 한 코스에 최대 2회까지만 추가할 수 있으며, 3회 이상 추가 시 400을 반환합니다. "
              + "존재하지 않거나 삭제된 코스, 또는 타인의 코스인 경우 404를 반환합니다. "
              + "존재하지 않거나 삭제된 장소인 경우 404를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 장소 추가 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                    {
                      "success": true,
                      "data": {
                        "coursePlaceId": 1,
                        "placeId": 10,
                        "order": 1,
                        "stayMinute": 60
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 유효성 오류",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "장소 식별자 누락 또는 유효하지 않음",
                      value =
                          """
                      {"success":false,"code":"INVALID_PLACE_ID","message":"장소 식별자는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "방문 순서 오류 (1 미만)",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "체류 시간 오류 (음수 또는 1440분 초과)",
                      value =
                          """
                      {"success":false,"code":"INVALID_STAY_MINUTE","message":"체류 시간은 0분 이상 1440분 이하이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "동일 장소 중복 횟수 초과 (최대 2회)",
                      value =
                          """
                      {"success":false,"code":"MAX_PLACE_DUPLICATION_EXCEEDED","message":"동일한 장소는 코스에 최대 2회까지만 추가할 수 있습니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 정보 누락, 유효하지 않은 토큰 또는 만료된 로그인 세션",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "인증 필요",
                        value =
                            """
                    {"success":false,"code":"AUTHENTICATION_REQUIRED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스 또는 장소를 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "코스를 찾을 수 없거나 작성자가 아님",
                      value =
                          """
                      {"success":false,"code":"COURSE_NOT_FOUND","message":"코스를 찾을 수 없습니다."}
                      """),
                  @ExampleObject(
                      name = "장소를 찾을 수 없거나 삭제됨",
                      value =
                          """
                      {"success":false,"code":"PLACE_NOT_FOUND","message":"장소를 찾을 수 없습니다."}
                      """)
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
  ResponseEntity<CommonResponse<AddCoursePlaceResponse>> addCoursePlace(
      @Parameter(description = "장소를 추가할 코스 식별자입니다.", example = "1", required = true) int courseId,
      AddCoursePlaceRequest request,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스 장소들의 방문 순서를 일괄 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 새로운 코스 장소 식별자 순서 목록
   * @param authentication 인증된 작성자 정보
   * @return 처리 성공 여부
   */
  @Operation(
      summary = "코스 장소 순서 일괄 변경",
      description =
          "코스에 포함된 장소들의 방문 순서(order)를 요청한 coursePlaceId 목록 순서대로 일괄 변경합니다. "
              + "작성자 본인만 변경할 수 있습니다. "
              + "요청의 coursePlaceIds 목록은 비어있지 않아야 하며, 기존 코스에 속한 모든 장소의 식별자와 중복 없이 정확히 일치해야 합니다. "
              + "불일치하거나 유효하지 않은 식별자가 포함된 경우 400을 반환합니다. "
              + "존재하지 않거나 삭제된 코스, 또는 타인의 코스인 경우 404를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "방문 순서 변경 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                    {
                      "success": true,
                      "data": null
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 유효성 오류 또는 방문 순서 불일치",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "목록 누락 또는 비어있음",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "기존 코스 장소 목록과 불일치",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 정보 누락, 유효하지 않은 토큰 또는 만료된 로그인 세션",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "인증 필요",
                        value =
                            """
                    {"success":false,"code":"AUTHENTICATION_REQUIRED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스를 찾을 수 없거나 작성자가 아님",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "코스를 찾을 수 없거나 작성자가 아님",
                        value =
                            """
                    {"success":false,"code":"COURSE_NOT_FOUND","message":"코스를 찾을 수 없습니다."}
                    """))),
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
  ResponseEntity<CommonResponse<Void>> updateCoursePlaceOrders(
      @Parameter(description = "순서를 변경할 코스 식별자입니다.", example = "1", required = true) int courseId,
      UpdateCoursePlaceOrdersRequest request,
      @Parameter(hidden = true) Authentication authentication);
}

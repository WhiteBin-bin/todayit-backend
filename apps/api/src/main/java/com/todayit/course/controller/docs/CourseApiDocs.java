package com.todayit.course.controller.docs;

import com.todayit.common.openapi.OpenApiConfig;
import com.todayit.common.response.CommonResponse;
import com.todayit.common.response.ErrorResponse;
import com.todayit.course.dto.request.AddCoursePlaceRequest;
import com.todayit.course.dto.request.CreateCourseRequest;
import com.todayit.course.dto.request.UpdateCoursePlaceOrdersRequest;
import com.todayit.course.dto.response.AddCoursePlaceResponse;
import com.todayit.course.dto.response.CopyCourseResponse;
import com.todayit.course.dto.response.CourseDetailResponse;
import com.todayit.course.dto.response.CreateCourseResponse;
import com.todayit.course.dto.response.RemoveCoursePlaceResponse;
import com.todayit.course.dto.response.UpdateCoursePlaceOrdersResponse;
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
              + "시작 일시(startAt)와 이동 수단(transport), 콘셉트 목록(themes)은 필수 입력 항목입니다. "
              + "콘셉트는 1개 이상 2개 이하로 중복 없이 선택해야 합니다. "
              + "종료 일시(endAt)는 선택 항목이며, 입력하는 경우 시작 일시보다 늦은 시간이어야 합니다.",
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
        description = "요청 파라미터 유효성 검증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "코스명 길이 미달(공백) 또는 초과(100자 초과)",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_TITLE","message":"코스명은 1자 이상 100자 이하이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "콘셉트 개수 오류(0개 또는 3개 이상) 또는 중복 콘셉트 입력",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_THEMES","message":"콘셉트는 중복 없이 1개 이상 2개 이하로 입력해야 합니다."}
                      """),
                  @ExampleObject(
                      name = "지원하지 않는 콘셉트 포함",
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
                        "sidoCode": "11",
                        "sidoName": "서울특별시",
                        "sigunguCode": "110",
                        "sigunguName": "종로구",
                        "themes": ["감성적", "여유로움"],
                        "isWriter": true,
                        "liked": false,
                        "scrapped": false,
                        "beforeVisit": ["웨이팅이 길 수 있으니 사전 예약을 권장합니다."],
                        "places": [
                          {
                            "coursePlaceId": 1,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "CAFE_DESSERT",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/images/place-1.jpg",
                            "order": 1,
                            "stayMinute": 60
                          }
                        ],
                        "createdAt": "2026-11-17T12:00:00",
                        "updatedAt": null
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스를 찾을 수 없거나 비공개 코스에 접근할 권한이 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "코스를 찾을 수 없음",
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
   * @param request 장소 추가 요청 본문
   * @param authentication 인증된 회원 정보
   * @return 추가된 장소 상세 및 최신 전체 장소 목록
   */
  @Operation(
      summary = "코스에 장소 추가",
      description =
          "코스에 새 장소를 추가합니다. "
              + "순서(order)를 지정하면 해당 위치에 장소가 삽입되고 기존 장소들의 순서가 1씩 밀립니다. "
              + "순서를 생략하면 코스의 맨 마지막 순서로 추가됩니다. "
              + "동일한 장소는 코스에 최대 2회까지만 추가할 수 있으며, 3회째 추가 시도 시 400(MAX_PLACE_DUPLICATION_EXCEEDED) 오류를 반환합니다. "
              + "존재하지 않거나 삭제된 장소 식별자를 추가하려 하면 404(PLACE_NOT_FOUND) 오류를 반환합니다. "
              + "코스 작성자만 장소를 추가할 수 있습니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "장소 추가 성공",
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
                        "addedPlace": {
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
                        },
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
                        ]
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "요청 파라미터 유효성 검증 실패 또는 동일 장소 중복 한도(2회) 초과",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "동일 장소 중복 한도(2회) 초과",
                      value =
                          """
                      {"success":false,"code":"MAX_PLACE_DUPLICATION_EXCEEDED","message":"동일한 장소는 코스에 최대 2회까지만 추가할 수 있습니다."}
                      """),
                  @ExampleObject(
                      name = "장소 식별자 유효성 오류(0 이하)",
                      value =
                          """
                      {"success":false,"code":"INVALID_PLACE_ID","message":"장소 식별자는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "방문 순서 유효성 오류(0 이하)",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "체류 시간 유효성 오류(음수 또는 1440분 초과)",
                      value =
                          """
                      {"success":false,"code":"INVALID_STAY_MINUTE","message":"체류 시간은 0분 이상 1440분 이하이어야 합니다."}
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
        description = "코스를 찾을 수 없거나(작성자 불일치 포함) 추가할 장소가 존재하지 않음",
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
  ResponseEntity<CommonResponse<AddCoursePlaceResponse>> addPlaceToCourse(
      @Parameter(description = "장소를 추가할 코스 식별자입니다.", example = "1", required = true) int courseId,
      AddCoursePlaceRequest request,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스 장소들의 방문 순서를 일괄 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 새로운 코스 장소 식별자 순서 목록
   * @param authentication 인증된 작성자 정보
   * @return 순서 변경 후 정렬된 전체 장소 목록
   */
  @Operation(
      summary = "코스 장소 순서 일괄 변경",
      description =
          "코스에 포함된 장소들의 방문 순서를 전달받은 식별자 순서대로 일괄 변경합니다. "
              + "코스에 등록된 모든 코스 장소 식별자(coursePlaceId)를 누락이나 중복 없이 정확히 전달해야 합니다. "
              + "작성자 본인만 순서를 변경할 수 있습니다. "
              + "전달된 식별자 목록이 기존 장소 목록과 일치하지 않으면 400(INVALID_COURSE_PLACE_ORDER) 오류를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "장소 순서 변경 성공",
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
                        "places": [
                          {
                            "coursePlaceId": 102,
                            "placeId": 20,
                            "name": "국립현대미술관",
                            "category": "CULTURE",
                            "address": "서울특별시 종로구 삼청로 30",
                            "latitude": 37.57883200,
                            "longitude": 126.98031200,
                            "imageUrl": "https://example.com/place2.jpg",
                            "order": 1,
                            "stayMinute": 90
                          },
                          {
                            "coursePlaceId": 101,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "CAFE_DESSERT",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/place1.jpg",
                            "order": 2,
                            "stayMinute": 60
                          }
                        ]
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "장소 식별자 목록이 비어 있거나, 개수 불일치 또는 기존 장소 목록과 불일치",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "장소 식별자 목록 불일치",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "목록이 비어 있음",
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
  ResponseEntity<CommonResponse<UpdateCoursePlaceOrdersResponse>> updateCoursePlaceOrders(
      @Parameter(description = "순서를 변경할 코스 식별자입니다.", example = "1", required = true) int courseId,
      UpdateCoursePlaceOrdersRequest request,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스에서 특정 장소를 삭제합니다.
   *
   * @param courseId 코스 식별자
   * @param coursePlaceId 삭제할 코스 장소 식별자
   * @param authentication 인증된 작성자 정보
   * @return 삭제된 장소 식별자 및 재조정된 남은 장소 목록
   */
  @Operation(
      summary = "코스 장소 삭제",
      description =
          "코스에서 특정 장소(coursePlaceId)를 삭제하고 이후 장소들의 방문 순서(order)를 자동으로 1씩 당깁니다. "
              + "삭제 성공 시 삭제된 식별자(deletedCoursePlaceId)와 재조정된 남은 장소 목록(places)을 반환합니다. "
              + "작성자 본인만 삭제할 수 있습니다. "
              + "마지막 장소를 삭제해도 빈 DRAFT 코스는 유지됩니다. "
              + "존재하지 않거나 삭제된 코스, 또는 타인의 코스인 경우 404(COURSE_NOT_FOUND)를 반환합니다. "
              + "해당 코스 내에 존재하지 않는 장소 식별자인 경우 404(COURSE_PLACE_NOT_FOUND)를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 장소 삭제 성공",
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
                        "deletedCoursePlaceId": 102,
                        "places": [
                          {
                            "coursePlaceId": 101,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "CAFE_DESSERT",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/place1.jpg",
                            "order": 1,
                            "stayMinute": 60
                          }
                        ]
                      }
                    }
                    """))),
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
        description = "코스를 찾을 수 없거나(작성자 불일치 포함) 코스 내 장소를 찾을 수 없음",
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
                      name = "코스 내 장소를 찾을 수 없음",
                      value =
                          """
                      {"success":false,"code":"COURSE_PLACE_NOT_FOUND","message":"코스 내 장소를 찾을 수 없습니다."}
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
  ResponseEntity<CommonResponse<RemoveCoursePlaceResponse>> deleteCoursePlace(
      @Parameter(description = "장소를 삭제할 코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(description = "삭제할 코스 장소 식별자입니다.", example = "10", required = true)
          int coursePlaceId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스를 영구 삭제합니다.
   *
   * @param courseId 삭제할 코스 식별자
   * @param authentication 인증된 작성자 정보
   * @return 처리 성공 여부
   */
  @Operation(
      summary = "코스 삭제",
      description =
          "작성자가 본인의 코스를 직접 영구 삭제(물리 삭제)합니다. "
              + "코스에 연결된 장소, 지역, 테마, 좋아요, 스크랩 등 모든 데이터가 함께 삭제되며 복구할 수 없습니다. "
              + "작성자 본인만 삭제할 수 있으며, 존재하지 않거나 이미 삭제된 코스 또는 타인의 코스인 경우 404를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "코스 영구 삭제 성공 (본문 없음)"),
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
  ResponseEntity<Void> deleteCourse(
      @Parameter(description = "삭제할 코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 확정된 원본 코스를 복사하여 새로운 DRAFT 코스를 생성합니다.
   *
   * @param courseId 복사할 원본 코스 식별자
   * @param authentication 인증된 요청 회원 정보
   * @return 생성된 DRAFT 코스 복사 응답
   */
  @Operation(
      summary = "코스 불러오기(복사)",
      description =
          "확정된 코스(공개 코스 또는 본인의 비공개 코스)를 복사하여 새로운 DRAFT 및 PRIVATE 상태의 코스를 생성합니다. "
              + "원본 코스의 장소 목록과 방문 순서는 유지되지만, 제목(title), 이동수단(transport), 방문 일정(startAt, endAt) 및 각 장소의 체류 시간(stayMinute)은 null(공백) 상태로 초기화됩니다. "
              + "DRAFT 상태인 코스는 복사할 수 없으며 400(CANNOT_COPY_DRAFT)을 반환합니다. "
              + "존재하지 않거나 삭제된 코스, 또는 타인의 비공개 코스는 존재 여부를 은닉하기 위해 404(COURSE_NOT_FOUND)를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 복사 성공",
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
                        "courseId": 4,
                        "originalCourseId": 1,
                        "title": null,
                        "status": "DRAFT",
                        "places": [
                          {
                            "coursePlaceId": 11,
                            "placeId": 6,
                            "name": "서울 전시 공간",
                            "category": "EXHIBITION_MUSEUM",
                            "address": "서울특별시 종로구 율곡로 30",
                            "latitude": 37.577,
                            "longitude": 126.981,
                            "imageUrl": "https://placehold.co/1200x800?text=Exhibition",
                            "order": 1,
                            "stayMinute": null
                          }
                        ],
                        "createdAt": "2026-11-19T17:32:45"
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "DRAFT 상태의 코스를 복사하려고 시도한 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "임시저장 코스 복사 불가",
                        value =
                            """
                    {"success":false,"code":"CANNOT_COPY_DRAFT","message":"임시저장 코스는 복사할 수 없습니다."}
                    """))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 필요",
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
        description = "코스를 찾을 수 없거나 타인의 비공개 코스에 접근한 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        name = "코스를 찾을 수 없음",
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
  ResponseEntity<CommonResponse<CopyCourseResponse>> copyCourse(
      @Parameter(description = "복사할 코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);
}

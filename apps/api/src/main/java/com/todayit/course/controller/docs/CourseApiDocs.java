package com.todayit.course.controller.docs;

import com.todayit.common.openapi.OpenApiConfig;
import com.todayit.common.response.CommonResponse;
import com.todayit.common.response.ErrorResponse;
import com.todayit.course.dto.request.AddCoursePlaceRequest;
import com.todayit.course.dto.request.CreateCourseRequest;
import com.todayit.course.dto.request.UpdateCourseConditionsRequest;
import com.todayit.course.dto.request.UpdateCoursePlaceOrdersRequest;
import com.todayit.course.dto.request.UpdateCourseTransportRequest;
import com.todayit.course.dto.request.UpdateCourseVisibilityRequest;
import com.todayit.course.dto.response.AddCoursePlaceResponse;
import com.todayit.course.dto.response.CopyCourseResponse;
import com.todayit.course.dto.response.CourseDetailResponse;
import com.todayit.course.dto.response.CourseLikeResponse;
import com.todayit.course.dto.response.CreateCourseResponse;
import com.todayit.course.dto.response.RemoveCoursePlaceResponse;
import com.todayit.course.dto.response.UpdateCourseConditionsResponse;
import com.todayit.course.dto.response.UpdateCoursePlaceOrdersResponse;
import com.todayit.course.dto.response.UpdateCourseTransportResponse;
import com.todayit.course.dto.response.UpdateCourseVisibilityResponse;
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
      summary = "코스 생성",
      description = "코스명, 콘셉트, 방문 시간, 이동수단을 입력받아 새로운 코스를 DRAFT 상태로 생성합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 생성 성공",
        useReturnTypeSchema = true,
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":true,"data":{"courseId":1}}
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "요청 유효성 검증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "코스명 누락 또는 길이 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_TITLE","message":"코스명은 1자 이상 100자 이하이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "콘셉트 개수 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_THEMES","message":"콘셉트는 중복 없이 1개 이상 2개 이하로 입력해야 합니다."}
                      """),
                  @ExampleObject(
                      name = "지원하지 않는 콘셉트",
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
                      name = "종료 시각 순서 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_END_AT","message":"종료 시각은 시작 시각 이후이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "이동수단 누락",
                      value =
                          """
                      {"success":false,"code":"COURSE_TRANSPORT_REQUIRED","message":"이동수단은 필수입니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<CommonResponse<CreateCourseResponse>> createCourse(
      CreateCourseRequest request, @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스 식별자로 코스 상세 정보를 조회합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 선택 인증 정보
   * @return 코스 상세 정보
   */
  @Operation(
      summary = "코스 상세 조회",
      description = "코스 식별자로 코스의 상세 정보, 연관된 장소 목록 및 방문 순서를 조회합니다.",
      security = {
        @SecurityRequirement(name = ""),
        @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
      })
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 상세 조회 성공",
        useReturnTypeSchema = true,
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
                        "title": "데이트 코스",
                        "courseSummary": "종로구 일대를 둘러보는 여유로운 코스입니다.",
                        "transport": "CAR",
                        "startAt": "2026-11-20T10:00:00",
                        "endAt": "2026-11-20T18:00:00",
                        "status": "ACTIVE",
                        "visibility": "PUBLIC",
                        "viewCount": 10,
                        "sidoCode": "11",
                        "sidoName": "서울특별시",
                        "sigunguCode": "110",
                        "sigunguName": "종로구",
                        "themes": ["카페·디저트"],
                        "isWriter": true,
                        "places": [
                          {
                            "coursePlaceId": 1,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "카페·디저트",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/place.jpg",
                            "order": 1,
                            "stayMinute": 60
                          }
                        ],
                        "createdAt": "2026-11-19T09:00:00",
                        "updatedAt": "2026-11-19T09:00:00",
                        "liked": false,
                        "scrapped": false,
                        "beforeVisit": []
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 존재하지 않거나 비공개 코스에 접근할 권한이 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<CommonResponse<CourseDetailResponse>> getCourseDetail(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스에 새 장소를 추가합니다.
   *
   * @param courseId 코스 식별자
   * @param request 장소 추가 요청 본문
   * @param authentication 인증된 작성자 정보
   * @return 추가된 장소 상세 및 최신 전체 장소 목록
   */
  @Operation(
      summary = "코스 장소 추가",
      description = "코스에 새로운 장소를 추가합니다. 동일한 장소는 코스에 최대 2회까지만 추가할 수 있습니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 장소 추가 성공",
        useReturnTypeSchema = true,
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
                          "coursePlaceId": 3,
                          "placeId": 10,
                          "name": "블루보틀 삼청",
                          "category": "카페·디저트",
                          "address": "서울특별시 종로구 북촌로 5길",
                          "latitude": 37.58123400,
                          "longitude": 126.98123400,
                          "imageUrl": "https://example.com/place.jpg",
                          "order": 3,
                          "stayMinute": 60
                        },
                        "places": [
                          {
                            "coursePlaceId": 1,
                            "placeId": 1,
                            "name": "경복궁",
                            "category": "명소·전망",
                            "address": "서울특별시 종로구 사직로 161",
                            "latitude": 37.57961700,
                            "longitude": 126.97704100,
                            "imageUrl": "https://example.com/place1.jpg",
                            "order": 1,
                            "stayMinute": 120
                          },
                          {
                            "coursePlaceId": 2,
                            "placeId": 2,
                            "name": "삼청동 수제비",
                            "category": "음식점",
                            "address": "서울특별시 종로구 삼청로 101-1",
                            "latitude": 37.58620000,
                            "longitude": 126.98220000,
                            "imageUrl": "https://example.com/place2.jpg",
                            "order": 2,
                            "stayMinute": 60
                          },
                          {
                            "coursePlaceId": 3,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "카페·디저트",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/place.jpg",
                            "order": 3,
                            "stayMinute": 60
                          }
                        ]
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "장소 식별자 또는 순서/체류시간 형식 오류, 혹은 동일 장소 중복 초과",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "장소 식별자 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_PLACE_ID","message":"장소 식별자는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "방문 순서 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "체류 시간 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_STAY_MINUTE","message":"체류 시간은 0분 이상 1440분 이하이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "장소 중복 횟수 초과",
                      value =
                          """
                      {"success":false,"code":"MAX_PLACE_DUPLICATION_EXCEEDED","message":"동일한 장소는 코스에 최대 2회까지만 추가할 수 있습니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 작성자가 아닌 경우, 또는 추가할 장소가 존재하지 않는 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "코스 없음 또는 작성자 불일치",
                      value =
                          """
                      {"success":false,"code":"COURSE_NOT_FOUND","message":"코스를 찾을 수 없습니다."}
                      """),
                  @ExampleObject(
                      name = "장소 없음",
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<CommonResponse<AddCoursePlaceResponse>> addPlaceToCourse(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
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
      description = "코스 내 장소들의 방문 순서를 전달받은 식별자 순서대로 일괄 변경합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 장소 순서 일괄 변경 성공",
        useReturnTypeSchema = true,
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
                            "coursePlaceId": 2,
                            "placeId": 2,
                            "name": "삼청동 수제비",
                            "category": "음식점",
                            "address": "서울특별시 종로구 삼청로 101-1",
                            "latitude": 37.58620000,
                            "longitude": 126.98220000,
                            "imageUrl": "https://example.com/place2.jpg",
                            "order": 1,
                            "stayMinute": 60
                          },
                          {
                            "coursePlaceId": 1,
                            "placeId": 1,
                            "name": "경복궁",
                            "category": "명소·전망",
                            "address": "서울특별시 종로구 사직로 161",
                            "latitude": 37.57961700,
                            "longitude": 126.97704100,
                            "imageUrl": "https://example.com/place1.jpg",
                            "order": 2,
                            "stayMinute": 120
                          }
                        ]
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "전달된 순서 목록이 비어 있거나 기존 장소 목록과 일치하지 않는 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"INVALID_COURSE_PLACE_ORDER","message":"방문 순서는 1 이상이어야 합니다."}
                    """))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 작성자가 아닌 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<CommonResponse<UpdateCoursePlaceOrdersResponse>> updateCoursePlaceOrders(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
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
      description = "코스 내 특정 장소를 삭제하고 남은 장소들의 순서를 재조정합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 장소 삭제 성공",
        useReturnTypeSchema = true,
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
                        "deletedCoursePlaceId": 2,
                        "places": [
                          {
                            "coursePlaceId": 1,
                            "placeId": 1,
                            "name": "경복궁",
                            "category": "명소·전망",
                            "address": "서울특별시 종로구 사직로 161",
                            "latitude": 37.57961700,
                            "longitude": 126.97704100,
                            "imageUrl": "https://example.com/place1.jpg",
                            "order": 1,
                            "stayMinute": 120
                          }
                        ]
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 작성자가 아닌 경우, 또는 삭제할 장소가 코스 내에 존재하지 않는 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "코스 없음 또는 작성자 불일치",
                      value =
                          """
                      {"success":false,"code":"COURSE_NOT_FOUND","message":"코스를 찾을 수 없습니다."}
                      """),
                  @ExampleObject(
                      name = "코스 내 장소 없음",
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<CommonResponse<RemoveCoursePlaceResponse>> deleteCoursePlace(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(description = "삭제할 코스 장소 식별자입니다.", example = "2", required = true)
          int coursePlaceId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 코스를 영구 삭제합니다.
   *
   * @param courseId 삭제할 코스 식별자
   * @param authentication 인증된 작성자 정보
   * @return 204 No Content
   */
  @Operation(
      summary = "코스 삭제",
      description = "코스를 영구 삭제합니다. 연관된 코스 장소, 지역 매핑, 테마 매핑 등의 데이터도 함께 삭제됩니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "코스 영구 삭제 성공"),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 작성자가 아닌 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<Void> deleteCourse(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 확정된 원본 코스를 바탕으로 새로운 DRAFT 코스를 생성합니다.
   *
   * @param courseId 복사할 원본 코스 식별자
   * @param authentication 인증된 회원 정보
   * @return 복제된 DRAFT 코스 정보
   */
  @Operation(
      summary = "코스 복사",
      description =
          "확정된 코스의 장소 목록과 순서만을 복제하여 새로운 DRAFT 코스를 생성합니다. 다른 회원의 공개 코스이거나 본인이 작성한 코스만 복사할 수 있습니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 복사 성공",
        useReturnTypeSchema = true,
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
                        "courseId": 2,
                        "originalCourseId": 1,
                        "title": null,
                        "status": "DRAFT",
                        "places": [
                          {
                            "coursePlaceId": 3,
                            "placeId": 10,
                            "name": "블루보틀 삼청",
                            "category": "카페·디저트",
                            "address": "서울특별시 종로구 북촌로 5길",
                            "latitude": 37.58123400,
                            "longitude": 126.98123400,
                            "imageUrl": "https://example.com/place.jpg",
                            "order": 1,
                            "stayMinute": null
                          }
                        ],
                        "createdAt": "2026-11-24T12:00:00"
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "임시저장(DRAFT) 상태의 코스를 복사하려는 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"CANNOT_COPY_DRAFT","message":"임시저장 코스는 복사할 수 없습니다."}
                    """))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "원본 코스가 없거나 삭제되었거나 타인의 비공개 코스인 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
                            """
                    {"success":false,"code":"INTERNAL_SERVER_ERROR","message":"서버 내부 오류가 발생했습니다."}
                    """)))
  })
  ResponseEntity<CommonResponse<CopyCourseResponse>> copyCourse(
      @Parameter(description = "복사할 원본 코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 인증된 회원이 코스에 좋아요를 등록합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 인증된 회원 정보
   * @return 코스 좋아요 결과
   */
  @Operation(
      summary = "코스 좋아요 등록",
      description = "코스에 좋아요를 1회 등록합니다. 이미 좋아요를 누른 경우 409 Conflict를 반환합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "코스 좋아요 등록 성공",
        useReturnTypeSchema = true,
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
                        "liked": true,
                        "likeCount": 10
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 비활성·삭제 상태이거나 접근 권한이 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"COURSE_NOT_FOUND","message":"코스를 찾을 수 없습니다."}
                    """))),
    @ApiResponse(
        responseCode = "409",
        description = "이미 좋아요를 누른 코스",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"COURSE_ALREADY_LIKED","message":"이미 좋아요를 눌렀습니다."}
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
  ResponseEntity<CommonResponse<CourseLikeResponse>> likeCourse(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 확정된 코스의 공개 범위를 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 공개 범위 변경 요청
   * @param authentication 인증된 작성자 정보
   * @return 변경된 공개 범위 정보
   */
  @Operation(
      summary = "코스 공개 범위 설정",
      description =
          "확정된 코스의 공개 범위(PUBLIC, PRIVATE)를 변경합니다. 작성자 본인만 변경할 수 있으며, 임시저장(DRAFT) 코스는 공개 범위를 변경할 수 없습니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 공개 범위 변경 성공",
        useReturnTypeSchema = true,
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
                        "visibility": "PUBLIC",
                        "updatedAt": "2026-11-24T12:00:00"
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 누락 또는 임시저장(DRAFT) 코스의 공개 범위 변경 시도",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "공개 범위 누락",
                      value =
                          """
                      {"success":false,"code":"INVALID_REQUEST","message":"공개 범위는 필수입니다."}
                      """),
                  @ExampleObject(
                      name = "임시저장 코스 변경 불가",
                      value =
                          """
                      {"success":false,"code":"CANNOT_CHANGE_DRAFT_VISIBILITY","message":"임시저장 코스는 공개 범위를 변경할 수 없습니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 삭제되었거나 작성자가 아닌 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
  ResponseEntity<CommonResponse<UpdateCourseVisibilityResponse>> updateCourseVisibility(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      UpdateCourseVisibilityRequest request,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 임시저장(DRAFT) 코스의 생성 조건을 수정합니다.
   *
   * @param courseId 코스 식별자
   * @param request 생성 조건 수정 요청 본문
   * @param authentication 인증된 회원 정보
   * @return 수정된 코스 생성 조건 및 장소 목록
   */
  @Operation(
      summary = "코스 생성 조건 수정",
      description = "임시저장(DRAFT) 코스의 생성 조건(행정 지역, 방문 시간, 소요 시간, 이동수단)을 수정합니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "코스 생성 조건 수정 성공",
        useReturnTypeSchema = true,
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
                        "conditions": {
                          "region": {
                            "sidoCode": "11",
                            "sidoName": "서울특별시",
                            "sigunguCode": "110",
                            "sigunguName": "종로구"
                          },
                          "startAt": "2026-12-01T10:00:00",
                          "endAt": "2026-12-01T14:00:00",
                          "durationMinutes": 240,
                          "transport": "CAR"
                        },
                        "places": [],
                        "updatedAt": "2026-11-24T18:30:00"
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "요청 유효성 검증 실패 또는 수정 불가 상태",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "지역 코드 형식 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_REGION","message":"시도 코드는 2자리, 시군구 코드는 3자리이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "종료 시각 순서 오류",
                      value =
                          """
                      {"success":false,"code":"INVALID_COURSE_END_AT","message":"종료 시각은 시작 시각 이후이어야 합니다."}
                      """),
                  @ExampleObject(
                      name = "확정 코스 수정 불가",
                      value =
                          """
                      {"success":false,"code":"CANNOT_EDIT_ACTIVE_COURSE","message":"확정된 코스는 생성 조건을 수정할 수 없습니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 삭제되었거나 작성자가 아닌 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
  ResponseEntity<CommonResponse<UpdateCourseConditionsResponse>> updateCourseConditions(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      UpdateCourseConditionsRequest request,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 임시저장(DRAFT) 코스의 이동수단을 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 이동수단 변경 요청 본문
   * @param authentication 인증된 작성자 정보
   * @return 변경된 이동수단 정보
   */
  @Operation(
      summary = "코스 이동수단 변경",
      description = "임시저장(DRAFT) 코스의 이동수단을 변경합니다. 확정된 코스는 이동수단을 변경할 수 없습니다.",
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "이동수단 변경 성공",
        useReturnTypeSchema = true,
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
                        "transport": "CAR"
                      }
                    }
                    """))),
    @ApiResponse(
        responseCode = "400",
        description = "요청 유효성 검증 실패 또는 수정 불가 상태",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "이동수단 누락",
                      value =
                          """
                      {"success":false,"code":"COURSE_TRANSPORT_REQUIRED","message":"이동수단은 필수입니다."}
                      """),
                  @ExampleObject(
                      name = "확정 코스 수정 불가",
                      value =
                          """
                      {"success":false,"code":"CANNOT_EDIT_ACTIVE_COURSE","message":"확정된 코스는 생성 조건을 수정할 수 없습니다."}
                      """)
                })),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            """
                    {"success":false,"code":"UNAUTHORIZED","message":"인증이 필요합니다."}
                    """))),
    @ApiResponse(
        responseCode = "404",
        description = "코스가 없거나 삭제되었거나 작성자가 아닌 경우",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
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
  ResponseEntity<CommonResponse<UpdateCourseTransportResponse>> updateCourseTransport(
      @Parameter(description = "코스 식별자입니다.", example = "1", required = true) int courseId,
      UpdateCourseTransportRequest request,
      @Parameter(hidden = true) Authentication authentication);
}

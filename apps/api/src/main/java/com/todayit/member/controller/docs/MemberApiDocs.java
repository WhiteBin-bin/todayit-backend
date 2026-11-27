package com.todayit.member.controller.docs;

import com.todayit.common.openapi.OpenApiConfig;
import com.todayit.common.pagination.PageResponse;
import com.todayit.common.response.CommonResponse;
import com.todayit.common.response.ErrorResponse;
import com.todayit.course.dto.response.MyCourseCardResponse;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.service.model.CourseScrapSort;
import com.todayit.course.service.model.MyCourseSort;
import com.todayit.member.dto.request.MemberProfileRequest;
import com.todayit.member.dto.response.MemberProfileDetailResponse;
import com.todayit.member.dto.response.MemberProfileImageResponse;
import com.todayit.member.dto.response.MemberProfileResponse;
import com.todayit.member.dto.response.NicknameAvailabilityResponse;
import com.todayit.place.dto.response.PlaceResponse;
import com.todayit.place.service.model.PlaceScrapSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

/** 회원의 닉네임, 스크랩 장소 및 프로필 관련 API 명세입니다. */
@Tag(name = "회원", description = "닉네임 확인, 스크랩 장소 및 회원 프로필 API")
public interface MemberApiDocs {

  /**
   * 닉네임의 사용 가능 여부를 확인합니다.
   *
   * @param nickname 확인할 닉네임
   * @return 닉네임 사용 가능 여부
   */
  @Operation(
      summary = "닉네임 중복 확인",
      description =
          """
          닉네임 사용 가능 여부를 확인합니다.

          - 앞뒤 공백을 제거한 후 길이(2~8자), 허용 문자(한글·영문·숫자), 금칙어를 검사합니다.
          - 이미 사용 중인 닉네임은 오류가 아닌 `available: false`로 반환합니다.
          """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "닉네임 확인 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                  @ExampleObject(
                      name = "사용 가능",
                      value = "{\"success\":true,\"data\":{\"available\":true}}"),
                  @ExampleObject(
                      name = "이미 사용 중",
                      value = "{\"success\":true,\"data\":{\"available\":false}}")
                })),
    @ApiResponse(
        responseCode = "400",
        description = "닉네임 누락, 형식 오류 또는 금칙어 사용",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "닉네임 누락",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\",\"message\":\"닉네임은 필수입니다.\"}"),
                  @ExampleObject(
                      name = "닉네임 길이 오류",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\",\"message\":\"닉네임은 2자 이상 8자 이하이어야 합니다.\"}"),
                  @ExampleObject(
                      name = "닉네임 형식 오류",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\",\"message\":\"닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.\"}"),
                  @ExampleObject(
                      name = "금칙어",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\",\"message\":\"사용할 수 없는 닉네임입니다.\"}")
                }))
  })
  ResponseEntity<CommonResponse<NicknameAvailabilityResponse>> checkNickname(
      @Parameter(
              description =
                  """
                  사용 가능 여부를 확인할 닉네임입니다.

                  - 앞뒤 공백을 제거한 뒤 2~8자여야 합니다.
                  - 한글, 영문, 숫자만 허용됩니다.
                  """,
              example = "오잇러버",
              required = true)
          String nickname);

  /**
   * 인증된 회원이 스크랩한 장소를 페이지 단위로 조회합니다.
   *
   * @param page 페이지 번호 (기본값 0)
   * @param sort 스크랩 정렬 기준 (기본값 LATEST)
   * @param authentication 인증된 회원 정보
   * @return 스크랩 장소 목록과 페이지 정보
   */
  @Operation(
      summary = "내 스크랩 장소 조회",
      description =
          """
          로그인한 회원이 스크랩한 장소 목록을 조회합니다.

          - `page`: 페이지 번호 (0부터 시작, 기본값 `0`, 페이지 크기는 `20` 고정)
          - `sort`: 정렬 기준 (`LATEST`: 최근 스크랩순, `OLDEST`: 오래된 스크랩순, 기본값 `LATEST`)
          - 비활성화되었거나 삭제된 장소는 목록에서 제외됩니다.
          """,
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "스크랩 장소 조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"content\":[{\"placeId\":1,\"name\":\"오늘의 식당\",\"latitude\":37.57,\"longitude\":126.985,\"address\":\"서울특별시 종로구 종로 1\",\"category\":\"RESTAURANT\",\"viewCount\":15,\"imageUrls\":[\"https://example.com/place.jpg\"]}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}}"))),
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
        responseCode = "401",
        description = "Access Token 인증 필요",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"인증이 필요합니다.\"}")))
  })
  ResponseEntity<CommonResponse<PageResponse<PlaceResponse>>> findScrappedPlaces(
      @Parameter(
              description =
                  """
                  페이지 번호입니다.

                  - 0부터 시작하며 생략하면 기본값은 `0`입니다.
                  - 페이지당 결과 개수(`size`)는 `20`으로 고정됩니다.
                  """,
              example = "0",
              schema = @Schema(minimum = "0", defaultValue = "0"))
          int page,
      @Parameter(
              description =
                  """
                  정렬 기준입니다.

                  - 생략하면 기본값은 `LATEST`입니다.
                  - `LATEST`: 최근 스크랩순
                  - `OLDEST`: 오래된 스크랩순
                  """,
              example = "LATEST",
              schema = @Schema(defaultValue = "LATEST"))
          PlaceScrapSort sort,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 인증된 회원의 프로필 이미지를 등록하거나 교체합니다.
   *
   * @param file 업로드할 이미지 파일
   * @param authentication 인증된 회원 정보
   * @return 변경된 프로필 이미지 정보
   */
  @Operation(
      summary = "프로필 이미지 등록 및 변경",
      description =
          """
                  로그인한 회원의 프로필 이미지를 등록하거나 교체합니다.

                  - 요청 형식: multipart/form-data
                  - 파일 필드명: file
                  - 업로드 파일 개수: 1개
                  - 허용 확장자: jpg, jpeg, png, webp
                  - 최대 파일 크기: 10MB
                  - 확장자, MIME 타입, 실제 파일 형식을 검증합니다.
                  - PROFILE_IMAGE 약관에 동의한 회원만 등록할 수 있습니다.
                  - 기존 이미지가 있으면 새 이미지로 교체합니다.
                  - 이미지 삭제는 DELETE /api/v1/members/me/profile-image를 사용합니다.
                  """,
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "프로필 이미지 등록 또는 교체 성공",
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
                                              "memberId": "123e4567-e89b-12d3-a456-426614174000",
                                              "profileImage": "https://example.com/profile.png",
                                              "updatedAt": "2026-11-13T08:00:00"
                                            }
                                          }
                                          """))),
    @ApiResponse(
        responseCode = "400",
        description = "파일 누락, 빈 파일, 파일 형식 오류, 크기 초과 또는 프로필 이미지 약관 미동의",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "파일 누락 또는 빈 파일",
                      value =
                          "{\"success\":false,\"code\":\"FILE_REQUIRED\",\"message\":\"업로드할 파일이 필요합니다.\"}"),
                  @ExampleObject(
                      name = "지원하지 않는 파일 형식",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_FILE_TYPE\",\"message\":\"지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp만 가능)\"}"),
                  @ExampleObject(
                      name = "파일 크기 초과",
                      value =
                          "{\"success\":false,\"code\":\"FILE_SIZE_EXCEEDED\",\"message\":\"파일 크기는 10MB 이하만 업로드할 수 있습니다.\"}"),
                  @ExampleObject(
                      name = "프로필 이미지 약관 미동의",
                      value =
                          "{\"success\":false,\"code\":\"PROFILE_IMAGE_CONSENT_REQUIRED\",\"message\":\"프로필 이미지 수집 및 이용 동의가 필요합니다.\"}")
                })),
    @ApiResponse(
        responseCode = "401",
        description = "Access Token 인증 필요",
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
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"MEMBER_NOT_FOUND\",\"message\":\"회원을 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "파일 읽기 실패 또는 외부 저장소 업로드 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "파일 읽기 실패",
                      value =
                          "{\"success\":false,\"code\":\"FILE_READ_FAILED\",\"message\":\"업로드 파일을 읽을 수 없습니다.\"}"),
                  @ExampleObject(
                      name = "파일 저장소 오류",
                      value =
                          "{\"success\":false,\"code\":\"FILE_STORAGE_FAILED\",\"message\":\"파일 저장소 작업에 실패했습니다.\"}")
                }))
  })
  ResponseEntity<CommonResponse<MemberProfileImageResponse>> uploadProfileImage(
      @Parameter(
              description =
                  """
                  업로드할 프로필 이미지 파일입니다.

                  - 허용 확장자: `jpg`, `jpeg`, `png`, `webp`
                  - 최대 파일 크기: `10MB`
                  """,
              required = true)
          MultipartFile file,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 로그인한 회원의 프로필 정보를 수정합니다.
   *
   * @param request 회원 프로필 수정 요청
   * @param authentication 인증된 회원 정보
   * @return 변경된 회원 프로필 정보
   */
  @Operation(
      summary = "회원 프로필 수정",
      description =
          """
                  로그인한 회원의 닉네임, 선호 지역 및 선호 테마를 수정합니다.

                  - 요청에서 생략하거나 null로 전달한 항목은 기존 값을 유지합니다.
                  - `preferredRegions`: 지정된 30개 지역 중 최대 1개 선택. 배열 안에 지역 코드 객체를 1개 전달합니다.
                  - `preferredRegions: []`: 선호 지역 선택 해제
                  - `preferredThemes`: 여러 테마 선택 가능. 중복 선택 불가
                  - `preferredThemes: []`: 모든 선호 테마 선택 해제
                  - `nickname`: 마지막 실제 변경 후 90일이 지나야 다시 변경할 수 있습니다.
                  - 선호 지역 또는 테마를 새로 저장하려면 PREFERENCE 약관 동의가 필요합니다.
                  - 프로필 이미지는 이 API에서 변경하지 않고 별도의 이미지 API를 사용합니다.
                  """
              + """

  선택 가능한 선호 지역 코드(시도/시군구):

  - 서울: 마포구(11/440), 성동구(11/200), 강남구(11/680), 송파구(11/710), 종로구(11/110), 용산구(11/170), 영등포구(11/560), 중구(11/140), 서초구(11/650), 광진구(11/215)
  - 경기: 수원시 팔달구(41/115), 성남시 분당구(41/135), 고양시 일산동구(41/285), 파주시(41/480), 가평군(41/820)
  - 인천: 연수구(28/185)
  - 부산: 수영구(26/500), 해운대구(26/350), 부산진구(26/230)
  - 강원: 강릉시(51/150), 속초시(51/210), 춘천시(51/110)
  - 제주: 제주시(50/110), 서귀포시(50/130)
  - 기타: 경주시(47/130), 전주시 완산구(52/111), 여수시(12/130), 대전 중구(30/140), 대구 중구(27/110), 천안시 동남구(44/131)
  """,
      requestBody =
          @RequestBody(
              required = true,
              description = "변경할 항목만 포함해 전달합니다.",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = MemberProfileRequest.class),
                      examples = {
                        @ExampleObject(
                            name = "선호 지역 1개 선택",
                            value =
                                """
                                                  {
                                                    "preferredRegions": [
                                                      {
                                                        "sidoCode": "11",
                                                        "sigunguCode": "440"
                                                      }
                                                    ]
                                                  }
                                                  """),
                        @ExampleObject(
                            name = "선호 지역 선택 해제",
                            value =
                                """
                                                  {
                                                    "preferredRegions": []
                                                  }
                                                  """),
                        @ExampleObject(
                            name = "선호 테마만 변경",
                            value =
                                """
                                                  {
                                                    "preferredThemes": ["QUIET", "ROMANTIC"]
                                                  }
                                                  """),
                        @ExampleObject(
                            name = "닉네임과 선호정보 함께 변경",
                            value =
                                """
                                                  {
                                                    "nickname": "새닉네임",
                                                    "preferredRegions": [
                                                      {
                                                        "sidoCode": "11",
                                                        "sigunguCode": "440"
                                                      }
                                                    ],
                                                    "preferredThemes": ["QUIET", "ROMANTIC"]
                                                  }
                                                  """)
                      })),
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "회원 프로필 수정 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":"
                                + "{\"memberId\":\"123e4567-e89b-12d3-a456-426614174000\","
                                + "\"nickname\":\"새닉네임\","
                                + "\"profileImage\":null,"
                                + "\"preferredRegions\":[{\"sidoCode\":\"11\","
                                + "\"sigunguCode\":\"110\"}],"
                                + "\"preferredThemes\":[\"QUIET\",\"ROMANTIC\"],"
                                + "\"updatedAt\":\"2026-11-09T04:00:00\"}}"))),
    @ApiResponse(
        responseCode = "400",
        description = "입력값 오류, 선호 지역 개수 초과, 닉네임 변경 주기 위반 또는 선호정보 약관 미동의",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                  @ExampleObject(
                      name = "수정 항목 누락",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"수정할 회원정보가 없습니다.\"}"),
                  @ExampleObject(
                      name = "선호 지역 개수 초과",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"선호 지역은 1개만 선택할 수 있습니다.\"}"),
                  @ExampleObject(
                      name = "닉네임 변경 주기 위반",
                      value =
                          "{\"success\":false,\"code\":\"NICKNAME_CHANGE_TOO_SOON\","
                              + "\"message\":\"닉네임은 마지막 변경 후 90일이 지나야 다시 변경할 수 있습니다.\"}"),
                  @ExampleObject(
                      name = "선호정보 약관 미동의",
                      value =
                          "{\"success\":false,\"code\":\"PREFERENCE_CONSENT_REQUIRED\","
                              + "\"message\":\"선호정보 저장을 위한 약관 동의가 필요합니다.\"}"),
                  @ExampleObject(
                      name = "선호 테마 중복",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"선호 테마를 중복 선택할 수 없습니다.\"}"),
                  @ExampleObject(
                      name = "잘못된 선호 테마 코드",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"요청값이 올바르지 않습니다.\"}"),
                  @ExampleObject(
                      name = "선호 지역 코드 형식 오류",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"선호 지역 코드가 올바르지 않습니다.\"}"),
                  @ExampleObject(
                      name = "닉네임 길이 오류",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"닉네임은 2자 이상 8자 이하이어야 합니다.\"}"),
                  @ExampleObject(
                      name = "닉네임 형식 오류",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REQUEST\","
                              + "\"message\":\"닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.\"}"),
                  @ExampleObject(
                      name = "존재하지 않거나 선택할 수 없는 선호 지역",
                      value =
                          "{\"success\":false,\"code\":\"INVALID_REGION_PATH\","
                              + "\"message\":\"지역 필터 값이 올바르지 않습니다.\"}")
                })),
    @ApiResponse(responseCode = "401", description = "Access Token 인증 필요"),
    @ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"MEMBER_NOT_FOUND\","
                                + "\"message\":\"회원을 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "409",
        description = "닉네임 중복",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"NICKNAME_ALREADY_EXISTS\","
                                + "\"message\":\"이미 사용 중인 닉네임입니다.\"}")))
  })
  ResponseEntity<CommonResponse<MemberProfileResponse>> updateProfile(
      MemberProfileRequest request, @Parameter(hidden = true) Authentication authentication);

  /**
   * 로그인한 회원 본인의 프로필 정보를 조회합니다.
   *
   * @param authentication 인증된 회원 정보
   * @return 회원 프로필 조회 응답
   */
  @Operation(
      summary = "회원 프로필 조회",
      description =
          """
          로그인한 회원 본인의 프로필 정보를 조회합니다.

          - 이메일은 본인 조회에서만 반환하며, 소셜 회원의 이메일은 `null`입니다.
          - 등록한 프로필 이미지가 없으면 `profileImage`는 `null`을 반환합니다.
          - 선호 지역과 선호 테마가 설정되지 않았으면 각각 빈 배열(`[]`)을 반환합니다.
          """,
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "회원 프로필 조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":"
                                + "{\"memberId\":\"123e4567-e89b-12d3-a456-426614174000\","
                                + "\"email\":\"test@example.com\","
                                + "\"nickname\":\"테스트회원\","
                                + "\"profileImage\":null,"
                                + "\"preferredRegions\":[],"
                                + "\"preferredThemes\":[],"
                                + "\"updatedAt\":\"2026-11-09T04:00:00\"}}"))),
    @ApiResponse(responseCode = "401", description = "Access Token 인증 필요"),
    @ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  ResponseEntity<CommonResponse<MemberProfileDetailResponse>> findProfile(
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 로그인한 회원의 프로필 이미지를 삭제합니다.
   *
   * @param authentication 인증된 회원 정보
   * @return 기본 이미지로 복원된 회원 프로필 정보
   */
  @Operation(
      summary = "프로필 이미지 삭제",
      description =
          """
          로그인한 회원 본인의 프로필 이미지를 삭제하고 기본 이미지로 복원합니다.

          - 별도의 요청 본문은 필요하지 않습니다.
          - 삭제 후 `profileImage`는 `null`로 반환하며, 프론트엔드에서 기본 이미지를 표시합니다.
          - 이미 기본 이미지인 경우에도 성공하며 기존 `updatedAt`을 유지합니다.
          - 닉네임과 선호 정보는 변경하지 않습니다.
          """,
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "프로필 이미지 삭제 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":"
                                + "{\"memberId\":\"123e4567-e89b-12d3-a456-426614174000\","
                                + "\"profileImage\":null,"
                                + "\"updatedAt\":\"2026-11-10T05:00:00\"}}"))),
    @ApiResponse(responseCode = "401", description = "Access Token 인증 필요"),
    @ApiResponse(
        responseCode = "404",
        description = "회원을 찾을 수 없음",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"MEMBER_NOT_FOUND\","
                                + "\"message\":\"회원을 찾을 수 없습니다.\"}"))),
    @ApiResponse(
        responseCode = "500",
        description = "외부 파일 저장소 삭제 실패",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,"
                                + "\"code\":\"FILE_STORAGE_FAILED\","
                                + "\"message\":\"파일 저장소 작업에 실패했습니다.\"}")))
  })
  ResponseEntity<CommonResponse<MemberProfileImageResponse>> deleteProfileImage(
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 인증된 회원이 작성한 코스 목록을 조회합니다.
   *
   * @param status 코스 상태 필터 (선택)
   * @param page 페이지 번호 (기본값 0)
   * @param sort 정렬 기준 (기본값 LATEST)
   * @param authentication 인증된 회원 정보
   * @return 내 코스 카드 목록과 페이지 정보
   */
  @Operation(
      summary = "내 코스 목록 조회",
      description =
          """
          로그인한 회원이 작성한 코스 목록을 조회합니다.

          - `status`: `DRAFT`(임시저장), `ACTIVE`(확정) 중 선택할 수 있으며 생략 시 삭제되지 않은 모든 코스를 조회합니다.
          - `page`: 페이지 번호 (0부터 시작, 기본값 `0`, 페이지 크기는 `20` 고정)
          - `sort`: 정렬 기준 (`LATEST`: 최신순, `OLDEST`: 오래된순, 기본값 `LATEST`)
          """,
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "내 코스 조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"content\":[{\"courseId\":1,\"title\":\"데이트 코스\",\"status\":\"ACTIVE\",\"visibility\":\"PUBLIC\",\"thumbnailUrl\":\"https://example.com/place.jpg\",\"placeCount\":3,\"sidoName\":\"서울특별시\",\"sigunguName\":\"종로구\",\"themes\":[\"카페·디저트\"],\"likeCount\":5,\"startAt\":\"2026-11-24T10:00:00\",\"endAt\":\"2026-11-24T18:00:00\",\"createdAt\":\"2026-11-24T09:00:00\",\"updatedAt\":\"2026-11-24T12:00:00\"}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}}"))),
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
        responseCode = "401",
        description = "Access Token 인증 필요",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"인증이 필요합니다.\"}")))
  })
  ResponseEntity<CommonResponse<PageResponse<MyCourseCardResponse>>> findMyCourses(
      @Parameter(
              description =
                  """
                  코스 상태 필터입니다.

                  - 생략하면 삭제되지 않은 전체 코스를 조회합니다.
                  - `DRAFT`: 임시저장 코스만 조회
                  - `ACTIVE`: 확정된 코스만 조회
                  """,
              example = "ACTIVE",
              schema = @Schema(allowableValues = {"DRAFT", "ACTIVE"}))
          CourseStatus status,
      @Parameter(
              description =
                  """
                  페이지 번호입니다.

                  - 0부터 시작하며 생략하면 기본값은 `0`입니다.
                  - 페이지당 결과 개수(`size`)는 `20`으로 고정됩니다.
                  """,
              example = "0",
              schema = @Schema(minimum = "0", defaultValue = "0"))
          int page,
      @Parameter(
              description =
                  """
                  정렬 기준입니다.

                  - 생략하면 기본값은 `LATEST`입니다.
                  - `LATEST`: 최신순 (최근 생성순)
                  - `OLDEST`: 오래된순 (오래된 생성순)
                  """,
              example = "LATEST",
              schema = @Schema(defaultValue = "LATEST"))
          MyCourseSort sort,
      @Parameter(hidden = true) Authentication authentication);

  /**
   * 인증된 회원이 스크랩한 코스 목록을 조회합니다.
   *
   * @param page 페이지 번호 (기본값 0)
   * @param sort 스크랩 정렬 기준 (기본값 LATEST)
   * @param authentication 인증된 회원 정보
   * @return 스크랩 코스 카드 목록과 페이지 정보
   */
  @Operation(
      summary = "내 스크랩 코스 조회",
      description =
          """
          로그인한 회원이 활성 상태로 스크랩한 코스 목록을 조회합니다.

          - `page`: 페이지 번호 (0부터 시작, 기본값 `0`, 페이지 크기는 `20` 고정)
          - `sort`: 정렬 기준 (`LATEST`: 최근 스크랩순, `OLDEST`: 오래된 스크랩순, 기본값 `LATEST`)
          - 타인의 비공개 코스이거나 임시저장·삭제된 코스는 목록에서 제외됩니다.
          """,
      security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "스크랩 코스 조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":true,\"data\":{\"content\":[{\"courseId\":1,\"title\":\"데이트 코스\",\"status\":\"ACTIVE\",\"visibility\":\"PUBLIC\",\"thumbnailUrl\":\"https://example.com/place.jpg\",\"placeCount\":3,\"sidoName\":\"서울특별시\",\"sigunguName\":\"종로구\",\"themes\":[\"카페·디저트\"],\"likeCount\":5,\"startAt\":\"2026-11-24T10:00:00\",\"endAt\":\"2026-11-24T18:00:00\",\"createdAt\":\"2026-11-24T09:00:00\",\"updatedAt\":\"2026-11-24T12:00:00\"}],\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}}"))),
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
        responseCode = "401",
        description = "Access Token 인증 필요",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples =
                    @ExampleObject(
                        value =
                            "{\"success\":false,\"code\":\"AUTHENTICATION_REQUIRED\",\"message\":\"인증이 필요합니다.\"}")))
  })
  ResponseEntity<CommonResponse<PageResponse<MyCourseCardResponse>>> findScrappedCourses(
      @Parameter(
              description =
                  """
                  페이지 번호입니다.

                  - 0부터 시작하며 생략하면 기본값은 `0`입니다.
                  - 페이지당 결과 개수(`size`)는 `20`으로 고정됩니다.
                  """,
              example = "0",
              schema = @Schema(minimum = "0", defaultValue = "0"))
          int page,
      @Parameter(
              description =
                  """
                  정렬 기준입니다.

                  - 생략하면 기본값은 `LATEST`입니다.
                  - `LATEST`: 최근 스크랩순
                  - `OLDEST`: 오래된 스크랩순
                  """,
              example = "LATEST",
              schema = @Schema(defaultValue = "LATEST"))
          CourseScrapSort sort,
      @Parameter(hidden = true) Authentication authentication);
}

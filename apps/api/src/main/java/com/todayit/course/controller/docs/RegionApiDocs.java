package com.todayit.course.controller.docs;

import com.todayit.common.response.CommonResponse;
import com.todayit.common.response.ErrorResponse;
import com.todayit.course.dto.response.RegionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** 행정 지역 API의 Swagger 문서를 정의하는 인터페이스입니다. */
@Tag(name = "지역", description = "행정 지역 메타데이터 조회 API")
public interface RegionApiDocs {

  /**
   * 전체 시·도, 시·군·구 및 세부 지역 목록을 3단계 계층 구조로 조회합니다.
   *
   * @return 시·도, 시·군·구 및 하위 세부 지역 목록
   */
  @Operation(
      summary = "전체 지역 목록 조회",
      description = "전국 시·도, 시·군·구 및 하위 세부 지역 목록을 3단계 계층(트리) 구조로 일괄 조회합니다. 필터 렌더링에 사용됩니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "전체 지역 목록 조회 성공",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CommonResponse.class),
                examples =
                    @ExampleObject(
                        name = "성공 예시",
                        value =
                            """
                            {
                              "success": true,
                              "data": [
                                {
                                  "sidoCode": "11",
                                  "sidoName": "서울특별시",
                                  "sigungus": [
                                    {
                                      "sigunguCode": "110",
                                      "sigunguName": "종로구",
                                      "subRegions": []
                                    },
                                    {
                                      "sigunguCode": "140",
                                      "sigunguName": "중구",
                                      "subRegions": []
                                    },
                                    {
                                      "sigunguCode": "680",
                                      "sigunguName": "강남구",
                                      "subRegions": []
                                    }
                                  ]
                                },
                                {
                                  "sidoCode": "41",
                                  "sidoName": "경기도",
                                  "sigungus": [
                                    {
                                      "sigunguCode": "110",
                                      "sigunguName": "수원시",
                                      "subRegions": [
                                        {
                                          "sigunguCode": "111",
                                          "sigunguName": "장안구"
                                        },
                                        {
                                          "sigunguCode": "113",
                                          "sigunguName": "권선구"
                                        },
                                        {
                                          "sigunguCode": "115",
                                          "sigunguName": "팔달구"
                                        },
                                        {
                                          "sigunguCode": "117",
                                          "sigunguName": "영통구"
                                        }
                                      ]
                                    },
                                    {
                                      "sigunguCode": "130",
                                      "sigunguName": "성남시",
                                      "subRegions": [
                                        {
                                          "sigunguCode": "131",
                                          "sigunguName": "수정구"
                                        },
                                        {
                                          "sigunguCode": "133",
                                          "sigunguName": "중원구"
                                        },
                                        {
                                          "sigunguCode": "135",
                                          "sigunguName": "분당구"
                                        }
                                      ]
                                    },
                                    {
                                      "sigunguCode": "150",
                                      "sigunguName": "의정부시",
                                      "subRegions": []
                                    }
                                  ]
                                }
                              ]
                            }
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
  ResponseEntity<CommonResponse<List<RegionResponse>>> getRegions();
}

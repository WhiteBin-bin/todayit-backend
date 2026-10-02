package com.todayit.member.controller;

import com.todayit.common.pagination.PageResponse;
import com.todayit.common.pagination.PaginationValidator;
import com.todayit.common.response.ApiResponse;
import com.todayit.place.dto.response.PlaceResponse;
import com.todayit.place.service.PlaceScrapQueryService;
import com.todayit.place.service.model.PlaceScrapSort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 회원의 장소 관련 HTTP 요청을 처리하는 Controller입니다. */
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;

  private final PlaceScrapQueryService placeScrapQueryService;

  /**
   * 회원 장소 조회에 필요한 Service를 받습니다.
   *
   * @param placeScrapQueryService 장소 스크랩 조회 Service 계약
   */
  public MemberController(PlaceScrapQueryService placeScrapQueryService) {
    this.placeScrapQueryService = placeScrapQueryService;
  }

  /**
   * 인증된 회원이 스크랩한 장소를 조회합니다.
   *
   * @param page 페이지 번호
   * @param sort 스크랩 장소 정렬 기준
   * @param authentication 인증된 회원 정보
   * @return 스크랩 장소 페이지
   */
  @GetMapping("/me/scrapped-places")
  public ResponseEntity<ApiResponse<PageResponse<PlaceResponse>>> findScrappedPlaces(
      @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
      @RequestParam(defaultValue = "LATEST") PlaceScrapSort sort,
      Authentication authentication) {
    PaginationValidator.validate(page, DEFAULT_SIZE);
    return ResponseEntity.ok(
        ApiResponse.success(
            PageResponse.from(
                placeScrapQueryService.findScrappedPlaces(
                    authentication.getName(), page, DEFAULT_SIZE, sort),
                PlaceResponse::from)));
  }
}

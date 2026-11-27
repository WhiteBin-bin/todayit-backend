package com.todayit.course.controller;

import com.todayit.common.pagination.PageResponse;
import com.todayit.common.pagination.PageResult;
import com.todayit.common.response.CommonResponse;
import com.todayit.course.controller.docs.CourseApiDocs;
import com.todayit.course.dto.request.AddCoursePlaceRequest;
import com.todayit.course.dto.request.CourseRecommendationRequest;
import com.todayit.course.dto.request.CourseSearchRequest;
import com.todayit.course.dto.request.CreateCourseRequest;
import com.todayit.course.dto.request.UpdateCourseConditionsRequest;
import com.todayit.course.dto.request.UpdateCoursePlaceOrdersRequest;
import com.todayit.course.dto.request.UpdateCourseTransportRequest;
import com.todayit.course.dto.request.UpdateCourseVisibilityRequest;
import com.todayit.course.dto.response.AddCoursePlaceResponse;
import com.todayit.course.dto.response.CancelCourseScrapResponse;
import com.todayit.course.dto.response.CopyCourseResponse;
import com.todayit.course.dto.response.CourseCardResponse;
import com.todayit.course.dto.response.CourseDetailResponse;
import com.todayit.course.dto.response.CourseLikeResponse;
import com.todayit.course.dto.response.CourseScrapResponse;
import com.todayit.course.dto.response.CreateCourseResponse;
import com.todayit.course.dto.response.RecommendedCoursesResponse;
import com.todayit.course.dto.response.RemoveCoursePlaceResponse;
import com.todayit.course.dto.response.UpdateCourseConditionsResponse;
import com.todayit.course.dto.response.UpdateCoursePlaceOrdersResponse;
import com.todayit.course.dto.response.UpdateCourseTransportResponse;
import com.todayit.course.dto.response.UpdateCourseVisibilityResponse;
import com.todayit.course.service.CourseFacade;
import com.todayit.course.service.CourseRecommendationFacade;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.CourseCardResult;
import com.todayit.course.service.model.RemoveCoursePlaceResult;
import com.todayit.course.service.model.UpdateCourseConditionsResult;
import com.todayit.course.service.model.UpdateCoursePlaceOrdersResult;
import com.todayit.course.service.model.UpdateCourseTransportResult;
import com.todayit.course.service.model.UpdateCourseVisibilityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 코스 API 요청을 처리합니다. */
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController implements CourseApiDocs {

  private static final Logger LOG = LoggerFactory.getLogger(CourseController.class);

  private final CourseFacade courseFacade;
  private final CourseRecommendationFacade recommendations;

  /**
   * 코스 생성 및 조회 조정 서비스를 받습니다.
   *
   * @param courseFacade 코스 Facade
   * @param recommendations 코스 추천 Facade
   */
  public CourseController(CourseFacade courseFacade, CourseRecommendationFacade recommendations) {
    this.courseFacade = courseFacade;
    this.recommendations = recommendations;
  }

  /**
   * 공개·확정 코스 목록을 필터, 정렬 및 페이징 조건에 맞게 조회합니다.
   *
   * @param request 코스 목록 및 필터 조회 조건
   * @return 공개 코스 카드 페이지 응답
   */
  @Override
  @GetMapping
  public ResponseEntity<CommonResponse<PageResponse<CourseCardResponse>>> findCourses(
      @ModelAttribute CourseSearchRequest request) {
    PageResult<CourseCardResult> result = courseFacade.findCourses(request.toCommand());
    PageResponse<CourseCardResponse> response = PageResponse.from(result, CourseCardResponse::from);
    LOG.info(
        "코스 목록 조회 완료: page={}, size={}, count={}",
        response.page(),
        response.size(),
        response.content().size());
    return ResponseEntity.ok(CommonResponse.success(response));
  }

  /**
   * 인증된 회원의 추천 코스를 조회합니다.
   *
   * @param request 추천 조회 필터
   * @param authentication 인증된 회원 정보
   * @return 추천 순서의 코스 목록
   */
  @Override
  @GetMapping("/recommendations")
  public CommonResponse<RecommendedCoursesResponse> recommend(
      @ModelAttribute CourseRecommendationRequest request, Authentication authentication) {
    RecommendedCoursesResponse response =
        RecommendedCoursesResponse.from(
            recommendations.recommend(authentication.getName(), request.toCommand()));
    LOG.info("추천 코스 조회 완료: count={}", response.courses().size());
    return CommonResponse.success(response);
  }

  /**
   * 새로운 코스를 DRAFT 상태로 생성합니다.
   *
   * @param request 코스 생성 요청 본문
   * @param authentication 인증된 회원 정보
   * @return 생성된 코스 식별자
   */
  @Override
  @PostMapping
  public ResponseEntity<CommonResponse<CreateCourseResponse>> createCourse(
      @RequestBody CreateCourseRequest request, Authentication authentication) {
    request.validate();
    CreateCourseResponse response =
        CreateCourseResponse.from(
            courseFacade.createCourse(authentication.getName(), request.toCommand()));
    LOG.info("코스 생성 완료: courseId={}", response.courseId());
    return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(response));
  }

  /**
   * 코스 식별자로 코스 상세 정보를 조회합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 선택 인증 정보
   * @return 코스 상세 정보
   */
  @Override
  @GetMapping("/{courseId}")
  public ResponseEntity<CommonResponse<CourseDetailResponse>> getCourseDetail(
      @PathVariable int courseId, Authentication authentication) {
    String memberId = authentication != null ? authentication.getName() : null;
    CourseDetailResponse response =
        CourseDetailResponse.from(courseFacade.getCourseDetail(courseId, memberId));
    LOG.info("코스 상세 조회 완료: courseId={}", courseId);
    return ResponseEntity.ok(CommonResponse.success(response));
  }

  /**
   * 코스에 새 장소를 추가합니다.
   *
   * @param courseId 코스 식별자
   * @param request 장소 추가 요청 본문
   * @param authentication 인증된 회원 정보
   * @return 추가된 장소 상세 및 최신 전체 장소 목록
   */
  @Override
  @PostMapping("/{courseId}/places")
  public ResponseEntity<CommonResponse<AddCoursePlaceResponse>> addPlaceToCourse(
      @PathVariable int courseId,
      @RequestBody AddCoursePlaceRequest request,
      Authentication authentication) {
    request.validate();
    AddCoursePlaceResponse response =
        AddCoursePlaceResponse.from(
            courseFacade.addPlaceToCourse(
                authentication.getName(),
                new AddCoursePlaceCommand(
                    courseId, request.placeId(), request.order(), request.resolveStayMinutes())));
    LOG.info("코스 장소 추가 완료: courseId={}, placeId={}", courseId, request.placeId());
    return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(response));
  }

  /**
   * 코스 장소들의 방문 순서를 일괄 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 새로운 코스 장소 식별자 순서 목록
   * @param authentication 인증된 작성자 정보
   * @return 순서 변경 후 정렬된 전체 장소 목록
   */
  @Override
  @PatchMapping("/{courseId}/places/order")
  public ResponseEntity<CommonResponse<UpdateCoursePlaceOrdersResponse>> updateCoursePlaceOrders(
      @PathVariable int courseId,
      @RequestBody UpdateCoursePlaceOrdersRequest request,
      Authentication authentication) {
    request.validate();
    UpdateCoursePlaceOrdersResult result =
        courseFacade.updateCoursePlaceOrders(
            authentication.getName(),
            new UpdateCoursePlaceOrdersCommand(courseId, request.coursePlaceIds()));
    LOG.info("코스 장소 순서 변경 완료: courseId={}", courseId);
    return ResponseEntity.ok(CommonResponse.success(UpdateCoursePlaceOrdersResponse.from(result)));
  }

  /**
   * 코스에서 특정 장소를 삭제합니다.
   *
   * @param courseId 코스 식별자
   * @param coursePlaceId 삭제할 코스 장소 식별자
   * @param authentication 인증된 작성자 정보
   * @return 삭제된 장소 식별자 및 재조정된 남은 장소 목록
   */
  @Override
  @DeleteMapping("/{courseId}/places/{coursePlaceId}")
  public ResponseEntity<CommonResponse<RemoveCoursePlaceResponse>> deleteCoursePlace(
      @PathVariable int courseId, @PathVariable int coursePlaceId, Authentication authentication) {
    RemoveCoursePlaceResult result =
        courseFacade.removePlaceFromCourse(authentication.getName(), courseId, coursePlaceId);
    LOG.info("코스 장소 삭제 완료: courseId={}, coursePlaceId={}", courseId, coursePlaceId);
    return ResponseEntity.ok(CommonResponse.success(RemoveCoursePlaceResponse.from(result)));
  }

  /**
   * 코스를 영구 삭제합니다.
   *
   * @param courseId 삭제할 코스 식별자
   * @param authentication 인증된 작성자 정보
   * @return 204 No Content
   */
  @Override
  @DeleteMapping("/{courseId}")
  public ResponseEntity<Void> deleteCourse(
      @PathVariable int courseId, Authentication authentication) {
    courseFacade.deleteCourse(authentication.getName(), courseId);
    LOG.info("코스 삭제 완료: courseId={}", courseId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 확정된 코스를 새로운 DRAFT 코스로 복사합니다.
   *
   * @param courseId 복사할 원본 코스 식별자
   * @param authentication 인증된 회원 정보
   * @return 복사되어 새로 생성된 코스 상세 정보
   */
  @Override
  @PostMapping("/{courseId}/copy")
  public ResponseEntity<CommonResponse<CopyCourseResponse>> copyCourse(
      @PathVariable int courseId, Authentication authentication) {
    CopyCourseResponse response =
        CopyCourseResponse.from(courseFacade.copyCourse(authentication.getName(), courseId));
    LOG.info("코스 복사 완료: originalCourseId={}, courseId={}", courseId, response.courseId());
    return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(response));
  }

  /**
   * 인증된 회원의 코스 좋아요를 등록합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 인증된 회원 정보
   * @return 코스 좋아요 결과
   */
  @Override
  @PostMapping("/{courseId}/like")
  public ResponseEntity<CommonResponse<CourseLikeResponse>> likeCourse(
      @PathVariable int courseId, Authentication authentication) {
    CourseLikeResponse response =
        CourseLikeResponse.from(courseFacade.likeCourse(courseId, authentication.getName()));
    LOG.info("코스 좋아요 등록 완료: courseId={}", courseId);
    return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(response));
  }

  /**
   * 인증된 회원의 코스 스크랩을 등록합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 인증된 회원 정보
   * @return 코스 스크랩 결과
   */
  @Override
  @PostMapping("/{courseId}/scrap")
  public ResponseEntity<CommonResponse<CourseScrapResponse>> scrapCourse(
      @PathVariable int courseId, Authentication authentication) {
    CourseScrapResponse response =
        CourseScrapResponse.from(courseFacade.scrapCourse(courseId, authentication.getName()));
    LOG.info("코스 스크랩 등록 완료: courseId={}", courseId);
    return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(response));
  }

  /**
   * 인증된 회원의 코스 스크랩을 취소합니다.
   *
   * @param courseId 코스 식별자
   * @param authentication 인증된 회원 정보
   * @return 코스 스크랩 취소 결과
   */
  @Override
  @DeleteMapping("/{courseId}/scrap")
  public ResponseEntity<CommonResponse<CancelCourseScrapResponse>> cancelCourseScrap(
      @PathVariable int courseId, Authentication authentication) {
    CancelCourseScrapResponse response =
        CancelCourseScrapResponse.from(
            courseFacade.cancelCourseScrap(courseId, authentication.getName()));
    LOG.info("코스 스크랩 취소 완료: courseId={}", courseId);
    return ResponseEntity.ok(CommonResponse.success(response));
  }

  /**
   * 확정된 코스의 공개 범위를 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 공개 범위 변경 요청 본문
   * @param authentication 인증된 작성자 정보
   * @return 변경된 공개 범위 정보
   */
  @Override
  @PatchMapping("/{courseId}/visibility")
  public ResponseEntity<CommonResponse<UpdateCourseVisibilityResponse>> updateCourseVisibility(
      @PathVariable int courseId,
      @RequestBody UpdateCourseVisibilityRequest request,
      Authentication authentication) {
    request.validate();
    UpdateCourseVisibilityResult result =
        courseFacade.updateCourseVisibility(
            authentication.getName(), courseId, request.visibility());
    LOG.info("코스 공개 범위 변경 완료: courseId={}", courseId);
    return ResponseEntity.ok(CommonResponse.success(UpdateCourseVisibilityResponse.from(result)));
  }

  /**
   * 임시저장(DRAFT) 코스의 생성 조건을 수정합니다.
   *
   * @param courseId 코스 식별자
   * @param request 생성 조건 수정 요청 본문
   * @param authentication 인증된 작성자 정보
   * @return 수정된 생성 조건 및 장소 목록
   */
  @Override
  @PatchMapping("/{courseId}/conditions")
  public ResponseEntity<CommonResponse<UpdateCourseConditionsResponse>> updateCourseConditions(
      @PathVariable int courseId,
      @RequestBody UpdateCourseConditionsRequest request,
      Authentication authentication) {
    request.validate();
    UpdateCourseConditionsResult result =
        courseFacade.updateCourseConditions(authentication.getName(), request.toCommand(courseId));
    LOG.info("코스 조건 수정 완료: courseId={}", courseId);
    return ResponseEntity.ok(CommonResponse.success(UpdateCourseConditionsResponse.from(result)));
  }

  /**
   * 임시저장(DRAFT) 코스의 이동수단을 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 이동수단 변경 요청 본문
   * @param authentication 인증된 작성자 정보
   * @return 변경된 이동수단 정보
   */
  @Override
  @PatchMapping("/{courseId}/transport")
  public ResponseEntity<CommonResponse<UpdateCourseTransportResponse>> updateCourseTransport(
      @PathVariable int courseId,
      @RequestBody UpdateCourseTransportRequest request,
      Authentication authentication) {
    request.validate();
    UpdateCourseTransportResult result =
        courseFacade.updateCourseTransport(authentication.getName(), courseId, request.transport());
    LOG.info("코스 이동수단 변경 완료: courseId={}", courseId);
    return ResponseEntity.ok(CommonResponse.success(UpdateCourseTransportResponse.from(result)));
  }
}

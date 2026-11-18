package com.todayit.course.controller;

import com.todayit.common.response.CommonResponse;
import com.todayit.course.controller.docs.CourseApiDocs;
import com.todayit.course.dto.request.AddCoursePlaceRequest;
import com.todayit.course.dto.request.CreateCourseRequest;
import com.todayit.course.dto.request.UpdateCoursePlaceOrdersRequest;
import com.todayit.course.dto.response.AddCoursePlaceResponse;
import com.todayit.course.dto.response.CourseDetailResponse;
import com.todayit.course.dto.response.CreateCourseResponse;
import com.todayit.course.service.CourseFacade;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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

  private final CourseFacade courseFacade;

  /**
   * 코스 생성 및 조회 조정 서비스를 받습니다.
   *
   * @param courseFacade 코스 Facade
   */
  public CourseController(CourseFacade courseFacade) {
    this.courseFacade = courseFacade;
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
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            CommonResponse.success(
                CreateCourseResponse.from(
                    courseFacade.createCourse(authentication.getName(), request.toCommand()))));
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
    String memberId = authentication == null ? null : authentication.getName();
    return ResponseEntity.ok(
        CommonResponse.success(
            CourseDetailResponse.from(courseFacade.getCourseDetail(courseId, memberId))));
  }

  /**
   * 코스에 새 장소를 추가합니다.
   *
   * @param courseId 코스 식별자
   * @param request 추가할 장소 정보
   * @param authentication 인증된 작성자 정보
   * @return 추가된 코스 장소 정보
   */
  @Override
  @PostMapping("/{courseId}/places")
  public ResponseEntity<CommonResponse<AddCoursePlaceResponse>> addCoursePlace(
      @PathVariable int courseId,
      @RequestBody AddCoursePlaceRequest request,
      Authentication authentication) {
    request.validate();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            CommonResponse.success(
                AddCoursePlaceResponse.from(
                    courseFacade.addPlaceToCourse(
                        authentication.getName(),
                        new AddCoursePlaceCommand(
                            courseId,
                            request.placeId(),
                            request.order(),
                            request.resolveStayMinute())))));
  }

  /**
   * 코스 장소들의 방문 순서를 일괄 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param request 새로운 코스 장소 식별자 순서 목록
   * @param authentication 인증된 작성자 정보
   * @return 처리 성공 여부
   */
  @Override
  @PatchMapping("/{courseId}/places/order")
  public ResponseEntity<CommonResponse<Void>> updateCoursePlaceOrders(
      @PathVariable int courseId,
      @RequestBody UpdateCoursePlaceOrdersRequest request,
      Authentication authentication) {
    request.validate();
    courseFacade.updateCoursePlaceOrders(
        authentication.getName(),
        new UpdateCoursePlaceOrdersCommand(courseId, request.coursePlaceIds()));
    return ResponseEntity.ok(CommonResponse.success(null));
  }
}

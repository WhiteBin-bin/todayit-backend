package com.todayit.example.course.controller;

import com.todayit.example.course.dto.request.CreateCourseRequest;
import com.todayit.example.course.dto.response.CourseResponse;
import com.todayit.example.course.service.CourseFacade;

/** 입력 검증과 HTTP 모델 변환을 보여 줍니다. 실제 Endpoint로 등록하지 않습니다. */
public class CourseController {
  private final CourseFacade courseFacade;

  /**
   * 회원 확인과 생성을 조정할 Facade를 받습니다.
   *
   * @param courseFacade 코스 생성 Facade
   */
  public CourseController(CourseFacade courseFacade) {
    this.courseFacade = courseFacade;
  }

  /**
   * 입력을 검증하고 업무 결과를 응답으로 변환합니다.
   *
   * @param authenticatedMemberId 요청 본문이 아닌 서버 인증 결과의 회원 식별자
   * @param request 코스 이름
   * @return 생성된 코스 응답
   */
  public CourseResponse create(String authenticatedMemberId, CreateCourseRequest request) {
    request.validate();
    return CourseResponse.from(courseFacade.create(request.toCommand(authenticatedMemberId)));
  }
}

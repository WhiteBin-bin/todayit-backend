package com.todayit.example.course.service;

import com.todayit.example.course.service.command.CreateCourseCommand;
import com.todayit.example.course.service.model.CourseResult;
import com.todayit.example.member.service.MemberQueryService;
import org.springframework.transaction.annotation.Transactional;

/** 회원 확인과 코스 생성을 조정합니다. Repository를 직접 사용하지 않습니다. */
public class CourseFacade {
  private final MemberQueryService memberQueryService;
  private final CourseCommandService courseCommandService;

  /**
   * 각 업무의 공개 Service를 받습니다.
   *
   * @param memberQueryService 같은 DB에서 회원을 확인하는 조회 계약
   * @param courseCommandService 코스 생성 Service
   */
  public CourseFacade(
      MemberQueryService memberQueryService, CourseCommandService courseCommandService) {
    this.memberQueryService = memberQueryService;
    this.courseCommandService = courseCommandService;
  }

  /**
   * 회원이 존재할 때 코스를 생성합니다.
   *
   * @param command 인증된 회원 식별자와 코스 이름
   * @return 생성 결과
   * @throws IllegalArgumentException 회원이 존재하지 않거나 이름이 없거나 공백일 때
   */
  @Transactional
  public CourseResult create(CreateCourseCommand command) {
    if (!memberQueryService.exists(command.memberId())) {
      throw new IllegalArgumentException("회원이 존재하지 않습니다.");
    }
    return courseCommandService.create(command.name());
  }
}

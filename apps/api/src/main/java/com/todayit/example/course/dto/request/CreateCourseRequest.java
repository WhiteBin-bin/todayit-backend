package com.todayit.example.course.dto.request;

import com.todayit.example.course.service.command.CreateCourseCommand;

/**
 * 코스 생성 입력입니다.
 *
 * @param name 코스 이름
 */
public record CreateCourseRequest(String name) {
  /**
   * 인증 결과와 요청 값을 업무 입력으로 묶습니다.
   *
   * @param memberId 서버에서 인증한 회원 식별자
   * @return HTTP와 무관한 생성 입력
   */
  public CreateCourseCommand toCommand(String memberId) {
    return new CreateCourseCommand(memberId, name);
  }

  /**
   * 웹 경계에서 필수 입력을 확인하여 잘못된 요청이 업무 실행으로 넘어가지 않게 합니다.
   *
   * @throws IllegalArgumentException 이름이 없거나 공백일 때
   */
  public void validate() {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("코스 이름은 필수입니다.");
    }
  }
}

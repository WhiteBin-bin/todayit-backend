package com.todayit.course.service.command;

import java.util.List;

/**
 * 코스 장소 순서 변경 조건입니다.
 *
 * @param courseId 대상 코스 식별자
 * @param coursePlaceIds 새로운 방문 순서대로 정렬된 코스 장소 식별자 목록
 */
public record UpdateCoursePlaceOrdersCommand(int courseId, List<Integer> coursePlaceIds) {

  /**
   * 컴팩트 생성자에서 방어적 복사를 수행합니다.
   *
   * @param courseId 대상 코스 식별자
   * @param coursePlaceIds 코스 장소 식별자 목록
   */
  public UpdateCoursePlaceOrdersCommand {
    coursePlaceIds = coursePlaceIds == null ? List.of() : List.copyOf(coursePlaceIds);
  }
}

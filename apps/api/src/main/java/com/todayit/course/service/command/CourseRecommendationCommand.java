package com.todayit.course.service.command;

import com.todayit.course.entity.CourseTransport;
import java.util.List;

/**
 * 추천 코스 조회 조건입니다.
 *
 * @param region 시·도 2자리 또는 시·도와 시·군·구를 합친 5자리 코드
 * @param themes 선택 콘셉트 이름 목록
 * @param durationMinutes 총 예상시간 상한(분), 생략 시 null
 * @param transport 이동수단, 생략 시 null
 */
public record CourseRecommendationCommand(
    String region, List<String> themes, Integer durationMinutes, CourseTransport transport) {
  /** 조건 목록을 방어적으로 복사합니다. */
  public CourseRecommendationCommand {
    themes = themes == null ? List.of() : List.copyOf(themes);
  }
}

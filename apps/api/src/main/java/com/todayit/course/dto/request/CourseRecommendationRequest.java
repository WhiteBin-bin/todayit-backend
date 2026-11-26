package com.todayit.course.dto.request;

import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.command.CourseRecommendationCommand;
import java.util.HashSet;
import java.util.List;

/**
 * 추천 코스 조회 필터입니다.
 *
 * @param region 시·도 2자리 또는 시·도와 시·군·구 5자리 코드
 * @param themes 콘셉트 이름, 최대 2개
 * @param durationMinutes 총 예상시간 상한(분), 양수
 * @param transport CAR, WALK, PUBLIC_TRANSIT 또는 기존 이동수단 코드
 */
public record CourseRecommendationRequest(
    String region, List<String> themes, Integer durationMinutes, String transport) {
  /**
   * 조회 조건을 검증하고 업무 명령으로 변환합니다.
   *
   * @return 검증된 추천 조회 조건
   * @throws InvalidRequestException 지역·콘셉트·시간·이동수단 형식이 유효하지 않은 경우
   */
  public CourseRecommendationCommand toCommand() {
    if (region != null && !region.matches("[0-9]{2}([0-9]{3})?")) {
      throw new InvalidRequestException("지역은 시·도 2자리 또는 시·도와 시·군·구 5자리 코드로 입력해야 합니다.");
    }
    List<String> selected = themes == null ? List.of() : themes;
    if (selected.size() > 2
        || selected.stream().anyMatch(t -> t == null || t.isBlank())
        || new HashSet<>(selected).size() != selected.size()) {
      throw new InvalidRequestException("콘셉트는 중복 없이 최대 2개까지 입력할 수 있습니다.");
    }
    if (durationMinutes != null && durationMinutes <= 0) {
      throw new InvalidRequestException("예상시간 상한은 양수이어야 합니다.");
    }
    CourseTransport selectedTransport = null;
    if (transport != null) {
      selectedTransport =
          switch (transport) {
            case "CAR" -> CourseTransport.CAR;
            case "WALK", "WALKING" -> CourseTransport.WALKING;
            case "PUBLIC_TRANSIT", "PUBLIC_TRANSPORT" -> CourseTransport.PUBLIC_TRANSPORT;
            default -> throw new InvalidRequestException("지원하지 않는 이동수단입니다.");
          };
    }
    return new CourseRecommendationCommand(region, selected, durationMinutes, selectedTransport);
  }
}

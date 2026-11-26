package com.todayit.course.service.command;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.model.CourseDurationFilter;
import com.todayit.course.service.model.CourseSort;
import com.todayit.region.service.model.RegionFilter;
import java.util.List;

/**
 * 코스 목록 및 필터 조회 조건입니다.
 *
 * @param regions 시·도 및 시·군·구 지역 필터 목록, 생략 시 빈 목록
 * @param themes 선택 콘셉트 이름 목록, 최대 2개
 * @param duration 소요 시간 구간 필터 (ALL, UNDER_2_HOURS, TWO_TO_FOUR_HOURS, OVER_4_HOURS)
 * @param transport 이동수단, 생략 시 null
 * @param sort 정렬 기준 (LATEST 또는 POPULAR)
 * @param page 페이지 번호 (0 이상)
 * @param size 페이지 크기 (1 이상)
 */
public record CourseSearchCommand(
    List<RegionFilter> regions,
    List<String> themes,
    CourseDurationFilter duration,
    CourseTransport transport,
    CourseSort sort,
    int page,
    int size) {

  /** 조건 목록을 방어적으로 복사합니다. */
  public CourseSearchCommand {
    regions = regions == null ? List.of() : List.copyOf(regions);
    themes = themes == null ? List.of() : List.copyOf(themes);
    duration = duration == null ? CourseDurationFilter.ALL : duration;
    sort = sort == null ? CourseSort.LATEST : sort;
  }
}

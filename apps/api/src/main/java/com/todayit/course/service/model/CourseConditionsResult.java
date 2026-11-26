package com.todayit.course.service.model;

import com.todayit.course.entity.CourseTransport;
import com.todayit.region.service.model.RegionSummaryResult;
import java.time.LocalDateTime;

/**
 * 코스 생성 조건 결과 모델입니다.
 *
 * @param region 코스 지역 요약 정보, 설정되지 않았으면 null
 * @param startAt 방문 시작 일시, 설정되지 않았으면 null
 * @param endAt 방문 종료 일시, 설정되지 않았으면 null
 * @param durationMinutes 총 소요 시간(분), 계산 불가 시 null
 * @param transport 이동수단, 설정되지 않았으면 null
 */
public record CourseConditionsResult(
    RegionSummaryResult region,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer durationMinutes,
    CourseTransport transport) {}

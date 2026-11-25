package com.todayit.course.service.command;

import com.todayit.course.entity.CourseTransport;
import java.time.LocalDateTime;

/**
 * 코스 생성 조건 수정 명령 모델입니다.
 *
 * @param courseId 코스 식별자
 * @param region 변경할 지역 코드, 변경하지 않으면 null
 * @param startAt 방문 시작 일시, 변경하지 않으면 null
 * @param endAt 방문 종료 일시, 변경하지 않으면 null
 * @param durationMinutes 소요 시간(분), 변경하지 않으면 null
 * @param transport 이동수단, 변경하지 않으면 null
 */
public record UpdateCourseConditionsCommand(
    int courseId,
    String region,
    LocalDateTime startAt,
    LocalDateTime endAt,
    Integer durationMinutes,
    CourseTransport transport) {}

package com.todayit.course.service.model;

import com.todayit.course.entity.CourseTransport;

/**
 * 코스 이동수단 변경 결과 모델입니다.
 *
 * @param courseId 코스 식별자
 * @param transport 변경된 이동수단
 */
public record UpdateCourseTransportResult(int courseId, CourseTransport transport) {}

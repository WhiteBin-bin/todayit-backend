package com.todayit.course.service.model;

import com.todayit.course.entity.CourseVisibility;
import java.time.LocalDateTime;

/**
 * 코스 공개 범위 설정 결과 모델입니다.
 *
 * @param courseId 코스 식별자
 * @param visibility 코스 공개 범위
 * @param updatedAt 수정 일시
 */
public record UpdateCourseVisibilityResult(
    int courseId, CourseVisibility visibility, LocalDateTime updatedAt) {}

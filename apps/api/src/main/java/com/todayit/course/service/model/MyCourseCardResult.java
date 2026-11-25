package com.todayit.course.service.model;

import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseVisibility;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 코스 조회 결과의 카드 모델입니다.
 *
 * @param courseId 코스 식별자
 * @param title 코스 제목
 * @param status 코스 상태
 * @param visibility 코스 공개 범위
 * @param thumbnailUrl 코스 대표 썸네일 이미지 URL
 * @param placeCount 포함된 장소 수
 * @param sidoName 시·도 이름
 * @param sigunguName 시·군·구 이름
 * @param themes 테마 목록
 * @param likeCount 좋아요 수
 * @param startAt 코스 시작 일시
 * @param endAt 코스 종료 일시
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 */
public record MyCourseCardResult(
    int courseId,
    String title,
    CourseStatus status,
    CourseVisibility visibility,
    String thumbnailUrl,
    int placeCount,
    String sidoName,
    String sigunguName,
    List<String> themes,
    long likeCount,
    LocalDateTime startAt,
    LocalDateTime endAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /** 방어적 복사를 수행합니다. */
  public MyCourseCardResult {
    themes = themes != null ? List.copyOf(themes) : List.of();
  }
}

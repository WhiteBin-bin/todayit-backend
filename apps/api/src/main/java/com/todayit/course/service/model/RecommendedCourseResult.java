package com.todayit.course.service.model;

import com.todayit.course.entity.CourseTransport;
import com.todayit.region.service.model.RegionSummaryResult;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 추천 코스 카드입니다.
 *
 * @param courseId 코스 식별자
 * @param title 저장된 코스 제목
 * @param courseSummary 저장된 소개글
 * @param thumbnailUrl 첫 방문 장소의 대표 이미지, 없으면 null
 * @param region 코스의 지역 요약
 * @param themes 저장된 콘셉트 이름
 * @param transport 이동수단
 * @param placeCount 방문 수
 * @param likeCount 좋아요 수
 * @param updatedAt 최근 공개 시각, 미확인 시 null
 */
public record RecommendedCourseResult(
    int courseId,
    String title,
    String courseSummary,
    String thumbnailUrl,
    RegionSummaryResult region,
    List<String> themes,
    CourseTransport transport,
    int placeCount,
    long likeCount,
    LocalDateTime updatedAt) {
  /** 콘셉트를 방어적으로 복사합니다. */
  public RecommendedCourseResult {
    themes = List.copyOf(themes);
  }
}

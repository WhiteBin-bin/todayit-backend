package com.todayit.course.service.model;

import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.region.service.model.RegionSummaryResult;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 목록 및 필터 조회 결과의 카드 모델입니다.
 *
 * @param courseId 코스 식별자
 * @param title 코스 제목
 * @param courseSummary 코스 소개글
 * @param thumbnailUrl 첫 방문 장소의 대표 이미지, 없으면 null
 * @param region 코스 지역 요약, 없으면 null
 * @param themes 콘셉트 이름 목록
 * @param durationMinutes 예상 소요 시간(분, endAt - startAt), 미확인 시 null
 * @param transport 이동수단
 * @param status 코스 상태
 * @param visibility 공개 범위
 * @param placeCount 포함된 방문 장소 수
 * @param likeCount 좋아요 수
 * @param createdAt 생성 일시
 * @param updatedAt 최근 수정·공개 일시, 없으면 null
 */
public record CourseCardResult(
    int courseId,
    String title,
    String courseSummary,
    String thumbnailUrl,
    RegionSummaryResult region,
    List<String> themes,
    Integer durationMinutes,
    CourseTransport transport,
    CourseStatus status,
    CourseVisibility visibility,
    int placeCount,
    long likeCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /** 콘셉트 목록을 방어적으로 복사합니다. */
  public CourseCardResult {
    themes = themes == null ? List.of() : List.copyOf(themes);
  }
}

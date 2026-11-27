package com.todayit.course.service.model;

import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 상세 조회 결과입니다.
 *
 * @param courseId 코스 식별자
 * @param title 코스명
 * @param courseSummary 코스 요약
 * @param authorDisplayName 작성자 공개 표시명
 * @param authorProfileImageUrl 공개 표시 가능한 작성자 프로필 이미지 URL, 없으면 null
 * @param transport 이동수단
 * @param startAt 코스 시작 일시
 * @param endAt 코스 종료 일시, 없으면 null
 * @param status 코스 상태
 * @param visibility 코스 공개 여부
 * @param viewCount 조회수
 * @param sidoCode 시·도 코드, 없으면 null
 * @param sidoName 시·도 이름, 없으면 null
 * @param sigunguCode 시·군·구 코드, 없으면 null
 * @param sigunguName 시·군·구 이름, 없으면 null
 * @param themes 콘셉트 목록
 * @param isWriter 요청 회원이 작성자인지 여부
 * @param places 포함된 장소 목록
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시, 없으면 null
 * @param liked 요청 회원의 좋아요 여부
 * @param scrapped 요청 회원의 스크랩 여부
 * @param beforeVisit AI 방문 전 유의사항 목록
 */
public record CourseDetailResult(
    int courseId,
    String title,
    String courseSummary,
    String authorDisplayName,
    String authorProfileImageUrl,
    CourseTransport transport,
    LocalDateTime startAt,
    LocalDateTime endAt,
    CourseStatus status,
    CourseVisibility visibility,
    int viewCount,
    String sidoCode,
    String sidoName,
    String sigunguCode,
    String sigunguName,
    List<String> themes,
    boolean isWriter,
    List<CoursePlaceDetailResult> places,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean liked,
    boolean scrapped,
    List<String> beforeVisit) {

  /**
   * 컬렉션을 방어적으로 복사합니다.
   *
   * @param courseId 코스 식별자
   * @param title 코스명
   * @param courseSummary 코스 요약
   * @param authorDisplayName 작성자 공개 표시명
   * @param authorProfileImageUrl 작성자 프로필 이미지 URL
   * @param transport 이동수단
   * @param startAt 코스 시작 일시
   * @param endAt 코스 종료 일시
   * @param status 코스 상태
   * @param visibility 코스 공개 여부
   * @param viewCount 조회수
   * @param sidoCode 시·도 코드
   * @param sidoName 시·도 이름
   * @param sigunguCode 시·군·구 코드
   * @param sigunguName 시·군·구 이름
   * @param themes 콘셉트 목록
   * @param isWriter 작성자 여부
   * @param places 장소 목록
   * @param createdAt 생성 일시
   * @param updatedAt 수정 일시
   * @param liked 좋아요 여부
   * @param scrapped 스크랩 여부
   * @param beforeVisit 방문 전 유의사항 목록
   */
  public CourseDetailResult {
    themes = themes == null ? List.of() : List.copyOf(themes);
    places = places == null ? List.of() : List.copyOf(places);
    beforeVisit = beforeVisit == null ? List.of() : List.copyOf(beforeVisit);
  }
}

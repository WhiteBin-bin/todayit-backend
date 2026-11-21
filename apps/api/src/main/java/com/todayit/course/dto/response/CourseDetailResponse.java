package com.todayit.course.dto.response;

import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.model.CourseDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 코스 상세 조회 API 응답입니다.
 *
 * @param courseId 코스 식별자
 * @param title 코스 이름, 없으면 null
 * @param courseSummary 코스 요약 설명, 없으면 null
 * @param transport 이동 수단, 없으면 null
 * @param startAt 코스 시작 일시, 없으면 null
 * @param endAt 코스 종료 일시, 없으면 null
 * @param status 코스 상태
 * @param visibility 코스 공개 여부
 * @param viewCount 조회수
 * @param sidoCode 시·도 코드, 없으면 null
 * @param sidoName 시·도 명칭, 없으면 null
 * @param sigunguCode 시·군·구 코드, 없으면 null
 * @param sigunguName 시·군·구 명칭, 없으면 null
 * @param themes 코스 콘셉트 목록
 * @param isWriter 현재 요청 회원이 코스 작성자인지 여부
 * @param liked 현재 요청 회원의 코스 좋아요 여부
 * @param scrapped 현재 요청 회원의 코스 스크랩 여부
 * @param beforeVisit AI 방문 전 유의사항 목록
 * @param places 코스에 포함된 장소 목록 (순서 오름차순)
 * @param createdAt 코스 생성 일시
 * @param updatedAt 코스 수정 일시, 없으면 null
 */
@Schema(description = "코스 상세 조회 응답")
public record CourseDetailResponse(
    @Schema(description = "코스 식별자입니다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        int courseId,
    @Schema(
            description = "코스 이름입니다. 임시저장 코스는 미설정 시 null입니다.",
            example = "서울 도심 산책 코스",
            nullable = true)
        String title,
    @Schema(
            description = "코스 요약 설명입니다. 등록된 설명이 없으면 null을 반환합니다.",
            example = "카페와 산책을 즐기는 코스",
            nullable = true)
        String courseSummary,
    @Schema(
            description = "코스의 이동 수단입니다. 임시저장 코스는 미설정 시 null입니다.",
            example = "WALKING",
            nullable = true)
        CourseTransport transport,
    @Schema(
            description = "코스 시작 일시입니다. 임시저장 코스는 미설정 시 null입니다.",
            example = "2026-12-01T10:00:00",
            nullable = true)
        LocalDateTime startAt,
    @Schema(
            description = "코스 종료 일시입니다. 미설정 시 null을 반환합니다.",
            example = "2026-12-01T14:00:00",
            nullable = true)
        LocalDateTime endAt,
    @Schema(
            description = "코스 상태입니다.",
            example = "DRAFT",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseStatus status,
    @Schema(
            description = "코스 공개 여부입니다.",
            example = "PRIVATE",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseVisibility visibility,
    @Schema(description = "코스 조회수입니다.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        int viewCount,
    @Schema(description = "시·도 코드입니다.", example = "11", nullable = true) String sidoCode,
    @Schema(description = "시·도 명칭입니다.", example = "서울특별시", nullable = true) String sidoName,
    @Schema(
            description = "시·군·구 코드입니다. 시·도 전체 대상인 경우 null을 반환합니다.",
            example = "110",
            nullable = true)
        String sigunguCode,
    @Schema(
            description = "시·군·구 명칭입니다. 시·도 전체 대상인 경우 null을 반환합니다.",
            example = "종로구",
            nullable = true)
        String sigunguName,
    @Schema(
            description = "코스 콘셉트 목록입니다.",
            example = "[\"감성적\", \"여유로움\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> themes,
    @Schema(
            description = "현재 요청 회원이 코스 작성자인지 여부입니다. 비회원인 경우 false입니다.",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean isWriter,
    @Schema(
            description = "현재 요청 회원의 코스 좋아요 여부입니다. 비회원인 경우 false입니다.",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean liked,
    @Schema(
            description = "현재 요청 회원의 코스 스크랩 여부입니다. 비회원인 경우 false입니다.",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
        boolean scrapped,
    @Schema(
            description = "AI 방문 전 유의사항 목록입니다. 없으면 빈 목록을 반환합니다.",
            example = "[\"웨이팅이 길 수 있으니 사전 예약을 권장합니다.\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> beforeVisit,
    @Schema(
            description = "코스에 포함된 장소 목록입니다. 방문 순서(order) 기준 오름차순으로 정렬됩니다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<CoursePlaceResponse> places,
    @Schema(
            description = "코스 생성 일시입니다.",
            example = "2026-11-17T12:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
    @Schema(
            description = "코스 수정 일시입니다. 수정된 적 없으면 null을 반환합니다.",
            example = "2026-11-17T12:30:00",
            nullable = true)
        LocalDateTime updatedAt) {

  /**
   * 컬렉션을 방어적으로 복사합니다.
   *
   * @param courseId 코스 식별자
   * @param title 코스 이름
   * @param courseSummary 코스 요약 설명
   * @param transport 이동 수단
   * @param startAt 코스 시작 일시
   * @param endAt 코스 종료 일시
   * @param status 코스 상태
   * @param visibility 코스 공개 여부
   * @param viewCount 조회수
   * @param sidoCode 시·도 코드
   * @param sidoName 시·도 명칭
   * @param sigunguCode 시·군·구 코드
   * @param sigunguName 시·군·구 명칭
   * @param themes 코스 콘셉트 목록
   * @param isWriter 작성자 여부
   * @param liked 좋아요 여부
   * @param scrapped 스크랩 여부
   * @param beforeVisit 방문 전 유의사항 목록
   * @param places 장소 목록
   * @param createdAt 생성 일시
   * @param updatedAt 수정 일시
   */
  public CourseDetailResponse {
    themes = themes == null ? List.of() : List.copyOf(themes);
    places = places == null ? List.of() : List.copyOf(places);
    beforeVisit = beforeVisit == null ? List.of() : List.copyOf(beforeVisit);
  }

  /**
   * 서비스 조회 결과를 API 응답 모델로 변환합니다.
   *
   * @param result 코스 상세 조회 결과
   * @return 코스 상세 API 응답
   */
  public static CourseDetailResponse from(CourseDetailResult result) {
    return new CourseDetailResponse(
        result.courseId(),
        result.title(),
        result.courseSummary(),
        result.transport(),
        result.startAt(),
        result.endAt(),
        result.status(),
        result.visibility(),
        result.viewCount(),
        result.sidoCode(),
        result.sidoName(),
        result.sigunguCode(),
        result.sigunguName(),
        result.themes(),
        result.isWriter(),
        result.liked(),
        result.scrapped(),
        result.beforeVisit(),
        result.places().stream().map(CoursePlaceResponse::from).toList(),
        result.createdAt(),
        result.updatedAt());
  }
}

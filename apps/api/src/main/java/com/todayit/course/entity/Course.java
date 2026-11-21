package com.todayit.course.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 회원이 만든 코스의 기본 정보와 상태를 나타냅니다. */
@Entity
@Table(name = "course")
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_id", nullable = false)
  private int courseId;

  @Column(name = "member_id", nullable = false, length = 36)
  private String memberId;

  @Column(name = "title", length = 100)
  private String title;

  @Column(name = "course_summary", length = 500)
  private String courseSummary;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "before_visit")
  private List<String> beforeVisit = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(name = "transport", length = 30)
  private CourseTransport transport;

  @Column(name = "start_at")
  private LocalDateTime startAt;

  @Column(name = "end_at")
  private LocalDateTime endAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CourseStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility", nullable = false, length = 20)
  private CourseVisibility visibility;

  @Column(name = "view_count", nullable = false)
  private int viewCount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CoursePlace> coursePlaces = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RegionCourse> regions = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CourseTheme> themes = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CourseMemberLike> likes = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CourseScrap> scraps = new ArrayList<>();

  protected Course() {}

  /**
   * 코스 엔티티를 생성합니다.
   *
   * @param memberId 작성자 식별자
   * @param title 코스명
   * @param transport 이동수단
   * @param startAt 방문 시작 시각
   * @param endAt 방문 종료 시각, 미입력 시 null
   * @param selectedThemes 검증된 콘셉트 목록
   * @param createdAt 서버 생성 시각
   */
  private Course(
      String memberId,
      String title,
      CourseTransport transport,
      LocalDateTime startAt,
      LocalDateTime endAt,
      List<Theme> selectedThemes,
      LocalDateTime createdAt) {
    this.memberId = memberId;
    this.title = title;
    this.transport = transport;
    this.startAt = startAt;
    this.endAt = endAt;
    this.viewCount = 0;
    this.status = CourseStatus.DRAFT;
    this.visibility = CourseVisibility.PRIVATE;
    this.createdAt = createdAt;
    if (selectedThemes != null) {
      selectedThemes.forEach(theme -> this.themes.add(CourseTheme.create(this, theme)));
    }
  }

  /**
   * 원본 코스로부터 복사본 코스 엔티티를 생성합니다.
   *
   * @param memberId 복사를 수행하는 회원 식별자
   * @param sourceCourse 복사할 원본 확정 코스
   * @param createdAt 생성 시각
   */
  private Course(String memberId, Course sourceCourse, LocalDateTime createdAt) {
    this.memberId = memberId;
    this.title = null;
    this.transport = null;
    this.startAt = null;
    this.endAt = null;
    this.viewCount = 0;
    this.status = CourseStatus.DRAFT;
    this.visibility = CourseVisibility.PRIVATE;
    this.createdAt = createdAt;

    sourceCourse.getCoursePlaces().stream()
        .sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder()))
        .forEach(
            sourcePlace -> {
              CoursePlace copiedPlace =
                  CoursePlace.create(this, sourcePlace.getPlaceId(), sourcePlace.getOrder(), null);
              this.coursePlaces.add(copiedPlace);
            });
  }

  /**
   * 입력 조건으로 비공개 임시 코스를 생성합니다.
   *
   * @param memberId 작성자 식별자
   * @param title 코스명
   * @param transport 이동수단
   * @param startAt 방문 시작 시각
   * @param endAt 방문 종료 시각, 미입력 시 null
   * @param selectedThemes 검증된 콘셉트 목록
   * @param createdAt 서버 생성 시각
   * @return 장소와 지역이 없는 비공개 DRAFT
   */
  public static Course createDraft(
      String memberId,
      String title,
      CourseTransport transport,
      LocalDateTime startAt,
      LocalDateTime endAt,
      List<Theme> selectedThemes,
      LocalDateTime createdAt) {
    return new Course(memberId, title, transport, startAt, endAt, selectedThemes, createdAt);
  }

  /**
   * 확정된 원본 코스를 바탕으로 새로운 DRAFT 복사본을 생성합니다.
   *
   * @param memberId 복사를 수행하는 회원 식별자
   * @param sourceCourse 복사할 원본 확정 코스
   * @param createdAt 생성 시각
   * @return 장소 순서가 복제된 DRAFT 코스
   */
  public static Course createCopyDraft(
      String memberId, Course sourceCourse, LocalDateTime createdAt) {
    return new Course(memberId, sourceCourse, createdAt);
  }

  public int getCourseId() {
    return courseId;
  }

  public String getMemberId() {
    return memberId;
  }

  public String getTitle() {
    return title;
  }

  public String getCourseSummary() {
    return courseSummary;
  }

  public List<String> getBeforeVisit() {
    return beforeVisit == null ? List.of() : Collections.unmodifiableList(beforeVisit);
  }

  public CourseTransport getTransport() {
    return transport;
  }

  public LocalDateTime getStartAt() {
    return startAt;
  }

  public LocalDateTime getEndAt() {
    return endAt;
  }

  public CourseStatus getStatus() {
    return status;
  }

  public CourseVisibility getVisibility() {
    return visibility;
  }

  public int getViewCount() {
    return viewCount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public List<RegionCourse> getRegions() {
    return regions;
  }

  public List<CourseTheme> getThemes() {
    return themes;
  }

  public List<CoursePlace> getCoursePlaces() {
    return coursePlaces;
  }

  public List<CourseMemberLike> getLikes() {
    return Collections.unmodifiableList(likes);
  }

  public List<CourseScrap> getScraps() {
    return Collections.unmodifiableList(scraps);
  }

  /**
   * 코스의 조회 정보를 반환합니다.
   *
   * @return 코스 조회 정보
   */
  public CourseSnapshot getSnapshot() {
    return new CourseSnapshot(courseId, title, courseSummary, transport, startAt, endAt, viewCount);
  }

  /**
   * 코스 조회에 필요한 정보를 담습니다.
   *
   * @param courseId 코스 식별자
   * @param title 코스명
   * @param courseSummary 코스 요약
   * @param transport 이동수단
   * @param startAt 코스 시작 시간
   * @param endAt 코스 종료 시간
   * @param viewCount 조회수
   */
  public record CourseSnapshot(
      int courseId,
      String title,
      String courseSummary,
      CourseTransport transport,
      LocalDateTime startAt,
      LocalDateTime endAt,
      int viewCount) {}
}

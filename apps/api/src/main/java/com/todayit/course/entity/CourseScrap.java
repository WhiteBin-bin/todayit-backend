package com.todayit.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/** 회원이 스크랩한 코스와 스크랩 상태를 나타냅니다. */
@Entity
@Table(
    name = "course_scrap",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_course_scrap_member_course",
            columnNames = {"member_id", "course_id"}))
public class CourseScrap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_scrap_id", nullable = false)
  private int courseScrapId;

  @Column(name = "member_id", nullable = false, length = 36)
  private String memberId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "is_delete", nullable = false)
  private boolean deleted;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  protected CourseScrap() {}

  /**
   * 코스 스크랩 식별자를 반환합니다.
   *
   * @return 코스 스크랩 식별자
   */
  public int getCourseScrapId() {
    return courseScrapId;
  }

  /**
   * 회원 식별자를 반환합니다.
   *
   * @return 회원 식별자
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * 스크랩된 코스를 반환합니다.
   *
   * @return 코스 엔티티
   */
  public Course getCourse() {
    return course;
  }

  /**
   * 스크랩 활성 여부를 반환합니다.
   *
   * @return 스크랩 활성 여부
   */
  public boolean isActive() {
    return active;
  }

  /**
   * 스크랩 삭제 여부를 반환합니다.
   *
   * @return 스크랩 삭제 여부
   */
  public boolean isDeleted() {
    return deleted;
  }

  /**
   * 생성 시각을 반환합니다.
   *
   * @return 생성 시각
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * 수정 시각을 반환합니다.
   *
   * @return 수정 시각
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * 삭제 시각을 반환합니다.
   *
   * @return 삭제 시각
   */
  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }
}

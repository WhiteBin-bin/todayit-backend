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

/** 회원이 좋아요를 누른 코스 정보를 나타냅니다. */
@Entity
@Table(
    name = "course_member_like",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_course_member_like_member_course",
            columnNames = {"member_id", "course_id"}))
public class CourseMemberLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_member_like_id", nullable = false)
  private int courseMemberLikeId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "member_id", nullable = false, length = 36)
  private String memberId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  protected CourseMemberLike() {}

  /**
   * 코스 회원 좋아요 식별자를 반환합니다.
   *
   * @return 코스 회원 좋아요 식별자
   */
  public int getCourseMemberLikeId() {
    return courseMemberLikeId;
  }

  /**
   * 좋아요가 등록된 코스를 반환합니다.
   *
   * @return 코스 엔티티
   */
  public Course getCourse() {
    return course;
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
   * 생성 시각을 반환합니다.
   *
   * @return 생성 시각
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}

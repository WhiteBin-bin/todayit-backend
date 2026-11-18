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
import java.time.LocalDateTime;

/** 회원이 스크랩한 코스와 스크랩 상태를 나타냅니다. */
@Entity
@Table(name = "course_scrap")
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
}

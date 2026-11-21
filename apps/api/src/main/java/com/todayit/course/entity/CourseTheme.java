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

/** 코스와 테마의 관계를 나타냅니다. */
@Entity
@Table(name = "course_theme")
public class CourseTheme {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_theme_id", nullable = false)
  private int courseThemeId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "theme_id", nullable = false)
  private Theme theme;

  protected CourseTheme() {}

  /**
   * 코스와 테마 관계를 생성합니다.
   *
   * @param course 코스 엔티티
   * @param theme 테마 엔티티
   */
  private CourseTheme(Course course, Theme theme) {
    this.course = course;
    this.theme = theme;
  }

  /**
   * 코스와 테마 관계를 생성합니다.
   *
   * @param course 코스 엔티티
   * @param theme 테마 엔티티
   * @return 생성된 CourseTheme 엔티티
   */
  public static CourseTheme create(Course course, Theme theme) {
    return new CourseTheme(course, theme);
  }

  /**
   * 연결된 테마 엔티티를 반환합니다.
   *
   * @return 테마 엔티티
   */
  public Theme getTheme() {
    return theme;
  }
}

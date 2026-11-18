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

/** 지역과 코스의 관계를 나타냅니다. */
@Entity
@Table(name = "region_course")
public class RegionCourse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "region_course_id", nullable = false)
  private int regionCourseId;

  @Column(name = "region_id", nullable = false)
  private int regionId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  protected RegionCourse() {}

  RegionCourse(Course course, int regionId) {
    this.course = course;
    this.regionId = regionId;
  }

  /**
   * 지역 식별자를 반환합니다.
   *
   * @return 지역 식별자
   */
  public int getRegionId() {
    return regionId;
  }
}

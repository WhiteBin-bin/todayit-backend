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

/** 코스에 포함된 장소와 방문 순서를 나타내는 관계 엔티티입니다. */
@Entity
@Table(
    name = "course_place",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_course_place_course_order",
            columnNames = {"course_id", "order"}))
public class CoursePlace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "course_place_id", nullable = false)
  private int coursePlaceId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(name = "place_id", nullable = false)
  private int placeId;

  @Column(name = "\"order\"", nullable = false)
  private int order;

  @Column(name = "stay_minute", nullable = false)
  private int stayMinute;

  protected CoursePlace() {}

  /**
   * 테스트 및 내부 생성을 위한 생성자입니다.
   *
   * @param course 코스 엔티티
   * @param placeId 장소 식별자
   * @param order 방문 순서
   * @param stayMinute 머무는 시간(분)
   */
  public CoursePlace(Course course, int placeId, int order, int stayMinute) {
    this.course = course;
    this.placeId = placeId;
    this.order = order;
    this.stayMinute = stayMinute;
  }

  public int getCoursePlaceId() {
    return coursePlaceId;
  }

  public int getPlaceId() {
    return placeId;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int getStayMinute() {
    return stayMinute;
  }

  public void setStayMinute(int stayMinute) {
    this.stayMinute = stayMinute;
  }
}

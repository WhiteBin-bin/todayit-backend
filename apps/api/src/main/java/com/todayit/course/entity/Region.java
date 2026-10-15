package com.todayit.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 코스와 장소를 분류하는 행정 지역을 나타냅니다. */
@Entity
@Table(name = "region")
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "region_id", nullable = false)
  private int regionId;

  @Column(name = "si", nullable = false, length = 20)
  private String si;

  @Column(name = "gun", nullable = false, length = 20)
  private String gun;

  @Column(name = "gu", nullable = false, length = 30)
  private String gu;

  protected Region() {}
}

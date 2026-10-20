package com.todayit.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 코스를 분류하는 테마를 나타냅니다. */
@Entity
@Table(name = "theme")
public class Theme {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "theme_id", nullable = false)
  private int themeId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  protected Theme() {}
}

package com.todayit.example.course.repository;

import com.todayit.example.course.entity.Course;

/** 코스 Aggregate Root의 저장 계약입니다. */
public interface CourseRepository {

  /**
   * 코스 Aggregate 전체를 저장합니다.
   *
   * @param course 저장할 코스
   * @return 저장된 코스
   */
  Course save(Course course);
}

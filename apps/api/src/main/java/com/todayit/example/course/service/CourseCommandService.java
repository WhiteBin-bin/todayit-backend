package com.todayit.example.course.service;

import com.todayit.example.course.entity.Course;
import com.todayit.example.course.repository.CourseRepository;
import com.todayit.example.course.service.model.CourseResult;
import java.util.function.Supplier;
import org.springframework.transaction.annotation.Transactional;

/** 코스 생성과 저장을 담당합니다. 회원 확인 등 다른 업무와의 조정은 Facade가 담당합니다. */
public class CourseCommandService {
  private final CourseRepository courseRepository;
  private final Supplier<String> courseIdGenerator;

  /**
   * 저장과 식별자 생성을 외부에서 받습니다.
   *
   * @param courseRepository 저장 계약
   * @param courseIdGenerator 테스트에서 제어할 수 있는 식별자 생성기
   */
  public CourseCommandService(
      CourseRepository courseRepository, Supplier<String> courseIdGenerator) {
    this.courseRepository = courseRepository;
    this.courseIdGenerator = courseIdGenerator;
  }

  /**
   * 코스를 생성하고 저장 결과를 반환합니다.
   *
   * @param name 비어 있지 않은 코스 이름
   * @return 저장된 코스 결과
   * @throws IllegalArgumentException 이름이 없거나 공백일 때
   */
  @Transactional
  public CourseResult create(String name) {
    Course course = Course.create(courseIdGenerator.get(), name);
    return CourseResult.from(courseRepository.save(course));
  }
}

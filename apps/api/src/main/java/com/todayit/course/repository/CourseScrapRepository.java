package com.todayit.course.repository;

import com.todayit.course.entity.CourseScrap;
import org.springframework.data.jpa.repository.JpaRepository;

/** 코스 스크랩 정보를 조회하고 저장하는 JPA Repository입니다. */
public interface CourseScrapRepository extends JpaRepository<CourseScrap, Integer> {

  /**
   * 회원이 코스를 활성 상태로 스크랩했는지 확인합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 활성 스크랩 존재 여부
   */
  boolean existsByMemberIdAndCourseCourseIdAndActiveTrueAndDeletedFalse(
      String memberId, int courseId);
}

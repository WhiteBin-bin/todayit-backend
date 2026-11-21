package com.todayit.course.repository;

import com.todayit.course.entity.CourseMemberLike;
import org.springframework.data.jpa.repository.JpaRepository;

/** 코스 좋아요 정보를 조회하고 저장하는 JPA Repository입니다. */
public interface CourseMemberLikeRepository extends JpaRepository<CourseMemberLike, Integer> {

  /**
   * 회원의 코스 좋아요 여부를 확인합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 좋아요 존재 여부
   */
  boolean existsByMemberIdAndCourseCourseId(String memberId, int courseId);
}

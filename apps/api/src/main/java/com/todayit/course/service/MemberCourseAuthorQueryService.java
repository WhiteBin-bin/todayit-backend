package com.todayit.course.service;

import com.todayit.course.service.model.CourseAuthorProfile;

/** 코스 상세 조회에서 사용하는 작성자 공개 프로필 조회 계약입니다. */
public interface MemberCourseAuthorQueryService {
  /**
   * 작성자 회원 식별자로 코스 상세에 표시할 공개 프로필 정보를 조회합니다.
   *
   * @param memberId 작성자 회원 식별자
   * @return 작성자 공개 표시명과 프로필 이미지 URL
   */
  CourseAuthorProfile getAuthorProfile(String memberId);
}

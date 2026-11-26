package com.todayit.course.service;

import com.todayit.common.exception.BusinessException;
import com.todayit.course.service.model.CourseRecommendationProfile;

/** 코스 추천에서 사용하는 회원 선호 조회 계약입니다. */
public interface MemberCourseRecommendationQueryService {
  /**
   * 인증된 회원의 개인화 동의와 선호지역을 조회합니다.
   *
   * @param memberId 인증된 회원 식별자
   * @return 회원 추천 선호 정보
   * @throws BusinessException 회원이 없거나 비활성 상태인 경우
   */
  CourseRecommendationProfile getRecommendationProfile(String memberId);
}

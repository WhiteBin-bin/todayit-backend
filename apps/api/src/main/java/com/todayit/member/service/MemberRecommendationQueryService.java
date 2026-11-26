package com.todayit.member.service;

import com.todayit.common.exception.BusinessException;
import com.todayit.course.service.MemberCourseRecommendationQueryService;
import com.todayit.course.service.model.CourseRecommendationProfile;
import com.todayit.member.entity.Member;
import com.todayit.member.exception.MemberErrorCode;
import com.todayit.member.repository.MemberAgreementRepository;
import com.todayit.member.repository.MemberRepository;
import com.todayit.region.entity.Region;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회원의 추천 선호와 개인정보 이용 동의를 조회합니다. */
@Service
public class MemberRecommendationQueryService implements MemberCourseRecommendationQueryService {
  private final MemberRepository members;
  private final MemberAgreementRepository agreements;

  /**
   * 회원과 동의 저장소를 받습니다.
   *
   * @param members 회원 저장소
   * @param agreements 회원 동의 저장소
   */
  public MemberRecommendationQueryService(
      MemberRepository members, MemberAgreementRepository agreements) {
    this.members = members;
    this.agreements = agreements;
  }

  /**
   * 회원의 선호정보 이용 동의와 선호지역을 조회합니다.
   *
   * @param memberId 인증된 회원 식별자
   * @return 동의 여부와 선호지역 코드
   * @throws BusinessException 회원이 없거나 비활성 상태인 경우
   */
  @Override
  @Transactional(readOnly = true)
  public CourseRecommendationProfile getRecommendationProfile(String memberId) {
    Member member =
        members
            .findById(memberId)
            .filter(Member::isActive)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    boolean consent = agreements.existsAgreedActiveAgreement(memberId, "PREFERENCE");
    Region region = consent ? member.getPreferredRegion() : null;
    return new CourseRecommendationProfile(
        consent,
        region == null ? null : region.getSnapshot().sidoCode(),
        region == null ? null : region.getSnapshot().sigunguCode());
  }
}

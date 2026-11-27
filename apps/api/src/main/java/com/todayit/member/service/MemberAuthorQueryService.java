package com.todayit.member.service;

import com.todayit.course.service.MemberCourseAuthorQueryService;
import com.todayit.course.service.model.CourseAuthorProfile;
import com.todayit.member.entity.Member;
import com.todayit.member.repository.MemberAgreementRepository;
import com.todayit.member.repository.MemberRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 코스 작성자의 공개 표시명과 프로필 이미지를 조회합니다. */
@Service
public class MemberAuthorQueryService implements MemberCourseAuthorQueryService {
  private static final String PROFILE_IMAGE_AGREEMENT_CODE = "PROFILE_IMAGE";

  private final MemberRepository members;
  private final MemberAgreementRepository agreements;

  /**
   * 회원 저장소와 약관 동의 저장소를 받습니다.
   *
   * @param members 회원 저장소
   * @param agreements 회원 약관 동의 저장소
   */
  public MemberAuthorQueryService(MemberRepository members, MemberAgreementRepository agreements) {
    this.members = members;
    this.agreements = agreements;
  }

  /**
   * 작성자 회원의 활성 상태와 프로필 이미지 약관 동의 여부를 확인하여 공개 프로필을 반환합니다.
   *
   * @param memberId 작성자 회원 식별자
   * @return 작성자 공개 표시명과 프로필 이미지 URL
   */
  @Override
  @Transactional(readOnly = true)
  public CourseAuthorProfile getAuthorProfile(String memberId) {
    if (memberId == null || memberId.isBlank()) {
      return CourseAuthorProfile.inactive();
    }
    Optional<Member> found = members.findById(memberId).filter(Member::isActive);
    if (found.isEmpty()) {
      return CourseAuthorProfile.inactive();
    }
    Member member = found.get();
    String registeredImage = member.getRegisteredProfileImage();
    String profileImageUrl = null;
    if (registeredImage != null
        && agreements.existsAgreedActiveAgreement(memberId, PROFILE_IMAGE_AGREEMENT_CODE)) {
      profileImageUrl = registeredImage;
    }
    return new CourseAuthorProfile(member.getNickname(), profileImageUrl);
  }
}

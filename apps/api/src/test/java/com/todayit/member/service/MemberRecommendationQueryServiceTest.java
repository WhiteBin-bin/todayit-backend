package com.todayit.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todayit.common.exception.BusinessException;
import com.todayit.course.service.model.CourseRecommendationProfile;
import com.todayit.member.entity.Member;
import com.todayit.member.exception.MemberErrorCode;
import com.todayit.member.repository.MemberAgreementRepository;
import com.todayit.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberRecommendationQueryServiceTest {
  @Mock private MemberRepository members;
  @Mock private MemberAgreementRepository agreements;
  @Mock private Member member;
  @InjectMocks private MemberRecommendationQueryService service;

  @Test
  void doesNotReadPreferencesWithoutConsent() {
    when(members.findById("member")).thenReturn(Optional.of(member));
    when(member.isActive()).thenReturn(true);
    when(agreements.existsAgreedActiveAgreement("member", "PREFERENCE")).thenReturn(false);

    CourseRecommendationProfile profile = service.getRecommendationProfile("member");

    assertThat(profile.personalizedConsent()).isFalse();
    assertThat(profile.sidoCode()).isNull();
    assertThat(profile.sigunguCode()).isNull();
    verify(member, never()).getPreferredRegion();
  }

  @Test
  void rejectsInactiveMembers() {
    when(members.findById("member")).thenReturn(Optional.of(member));
    when(member.isActive()).thenReturn(false);

    BusinessException exception =
        assertThrows(BusinessException.class, () -> service.getRecommendationProfile("member"));

    assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    verify(agreements, never()).existsAgreedActiveAgreement("member", "PREFERENCE");
  }
}

package com.todayit.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayit.course.service.model.CourseAuthorProfile;
import com.todayit.member.entity.Member;
import com.todayit.member.repository.MemberAgreementRepository;
import com.todayit.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberAuthorQueryServiceTest {

  @Mock private MemberRepository members;
  @Mock private MemberAgreementRepository agreements;

  @InjectMocks private MemberAuthorQueryService service;

  @Test
  @DisplayName("활성 작성자가 프로필 이미지를 등록하고 약관에 동의했으면 닉네임과 이미지 URL을 반환한다")
  void returnsNicknameAndProfileImageWhenActiveAndAgreed() {
    // given
    Member member = mock(Member.class);
    when(member.isActive()).thenReturn(true);
    when(member.getNickname()).thenReturn("오잇러버");
    when(member.getRegisteredProfileImage()).thenReturn("https://example.com/profile.jpg");
    when(members.findById("member-1")).thenReturn(Optional.of(member));
    when(agreements.existsAgreedActiveAgreement("member-1", "PROFILE_IMAGE")).thenReturn(true);

    // when
    CourseAuthorProfile result = service.getAuthorProfile("member-1");

    // then
    assertThat(result.displayName()).isEqualTo("오잇러버");
    assertThat(result.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");
  }

  @Test
  @DisplayName("활성 작성자가 프로필 이미지 약관에 동의하지 않았으면 닉네임과 null 이미지 URL을 반환한다")
  void returnsNullProfileImageWhenActiveButNotAgreed() {
    // given
    Member member = mock(Member.class);
    when(member.isActive()).thenReturn(true);
    when(member.getNickname()).thenReturn("오잇러버");
    when(member.getRegisteredProfileImage()).thenReturn("https://example.com/profile.jpg");
    when(members.findById("member-1")).thenReturn(Optional.of(member));
    when(agreements.existsAgreedActiveAgreement("member-1", "PROFILE_IMAGE")).thenReturn(false);

    // when
    CourseAuthorProfile result = service.getAuthorProfile("member-1");

    // then
    assertThat(result.displayName()).isEqualTo("오잇러버");
    assertThat(result.profileImageUrl()).isNull();
  }

  @Test
  @DisplayName("활성 작성자의 등록된 프로필 이미지가 없으면 약관 조회 없이 null 이미지 URL을 반환한다")
  void returnsNullProfileImageWhenNoRegisteredImage() {
    // given
    Member member = mock(Member.class);
    when(member.isActive()).thenReturn(true);
    when(member.getNickname()).thenReturn("오잇러버");
    when(member.getRegisteredProfileImage()).thenReturn(null);
    when(members.findById("member-1")).thenReturn(Optional.of(member));

    // when
    CourseAuthorProfile result = service.getAuthorProfile("member-1");

    // then
    assertThat(result.displayName()).isEqualTo("오잇러버");
    assertThat(result.profileImageUrl()).isNull();
    verifyNoInteractions(agreements);
  }

  @Test
  @DisplayName("탈퇴·제재로 비활성화된 작성자는 ToDayIt 표시명과 null 이미지 URL을 반환한다")
  void returnsInactiveProfileWhenMemberIsInactive() {
    // given
    Member member = mock(Member.class);
    when(member.isActive()).thenReturn(false);
    when(members.findById("member-1")).thenReturn(Optional.of(member));

    // when
    CourseAuthorProfile result = service.getAuthorProfile("member-1");

    // then
    assertThat(result.displayName()).isEqualTo("ToDayIt");
    assertThat(result.profileImageUrl()).isNull();
    verifyNoInteractions(agreements);
  }

  @Test
  @DisplayName("작성자 회원 정보가 없거나 식별자가 비어 있으면 ToDayIt 표시명과 null 이미지 URL을 반환한다")
  void returnsInactiveProfileWhenMemberNotFoundOrBlank() {
    // given
    when(members.findById("missing-member")).thenReturn(Optional.empty());

    // when
    CourseAuthorProfile missingResult = service.getAuthorProfile("missing-member");
    CourseAuthorProfile blankResult = service.getAuthorProfile(" ");

    // then
    assertThat(missingResult.displayName()).isEqualTo("ToDayIt");
    assertThat(missingResult.profileImageUrl()).isNull();
    assertThat(blankResult.displayName()).isEqualTo("ToDayIt");
    assertThat(blankResult.profileImageUrl()).isNull();
    verifyNoInteractions(agreements);
  }
}

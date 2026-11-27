package com.todayit.course.service.model;

/**
 * 코스 작성자의 공개 프로필 정보입니다.
 *
 * @param displayName 작성자 공개 표시명 (활성 회원은 현재 닉네임, 탈퇴·제재 회원은 ToDayIt)
 * @param profileImageUrl 공개 표시 가능한 작성자 프로필 이미지 URL (미등록·삭제·비공개·비활성 회원은 null)
 */
public record CourseAuthorProfile(String displayName, String profileImageUrl) {
  /** 탈퇴·제재 등으로 비활성 상태인 작성자의 기본 공개 표시명입니다. */
  public static final String INACTIVE_AUTHOR_DISPLAY_NAME = "ToDayIt";

  /**
   * 비활성 또는 존재하지 않는 작성자의 기본 프로필 정보를 반환합니다.
   *
   * @return 기본 표시명(ToDayIt)과 null 프로필 이미지가 설정된 프로필
   */
  public static CourseAuthorProfile inactive() {
    return new CourseAuthorProfile(INACTIVE_AUTHOR_DISPLAY_NAME, null);
  }
}

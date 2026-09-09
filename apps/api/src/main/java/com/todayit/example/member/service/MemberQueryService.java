package com.todayit.example.member.service;

/** 회원 Entity나 Repository를 노출하지 않는 조회 계약입니다. */
public interface MemberQueryService {
  /**
   * 회원 존재 여부만 확인합니다. 인증이나 코스 접근 권한 검사를 대신하지 않습니다.
   *
   * @param memberId 확인할 회원 식별자
   * @return 회원이 존재하면 true
   */
  boolean exists(String memberId);
}

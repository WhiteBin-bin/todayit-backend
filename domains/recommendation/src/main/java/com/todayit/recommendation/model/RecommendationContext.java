package com.todayit.recommendation.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** 추천 요청 시 전달되는 사용자의 선호 정보, 선택 옵션 및 개인화 동의 상태를 담는 모델입니다. */
public class RecommendationContext {

  private final String memberId;
  private final boolean personalizedConsent;
  private final String preferredDongCode;
  private final Set<String> selectedConcepts;
  private final boolean hasHistory;

  /**
   * 추천 컨텍스트를 생성합니다.
   *
   * @param memberId 회원 식별자 (비회원은 null 가능)
   * @param personalizedConsent 개인화 추천 동의 여부 (철회 시 false)
   * @param preferredDongCode 저장된 선호지역 동 코드 (미설정 시 null)
   * @param selectedConcepts 요청에서 선택한 콘셉트 목록 (null 허용, 빈 Set 처리)
   * @param hasHistory 최근 30일 내 개인 활동(조회/좋아요/스크랩/계획) 이력 존재 여부
   */
  public RecommendationContext(
      String memberId,
      boolean personalizedConsent,
      String preferredDongCode,
      Set<String> selectedConcepts,
      boolean hasHistory) {
    this.memberId = memberId;
    this.personalizedConsent = personalizedConsent;
    this.preferredDongCode = preferredDongCode;
    this.selectedConcepts =
        selectedConcepts == null
            ? Collections.emptySet()
            : Collections.unmodifiableSet(new HashSet<>(selectedConcepts));
    this.hasHistory = hasHistory;
  }

  /**
   * 회원 여부를 반환합니다.
   *
   * @return 회원이면 true, 비회원이면 false
   */
  public boolean isMember() {
    return memberId != null && !memberId.isBlank();
  }

  /**
   * 회원 식별자를 반환합니다.
   *
   * @return 회원 식별자
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * 개인화 추천 동의 여부를 반환합니다.
   *
   * @return 동의 시 true, 철회 시 false
   */
  public boolean isPersonalizedConsent() {
    return personalizedConsent;
  }

  /**
   * 저장된 선호지역 동 코드를 반환합니다.
   *
   * @return 선호지역 동 코드 (미설정 시 null)
   */
  public String getPreferredDongCode() {
    return preferredDongCode;
  }

  /**
   * 선호지역 동 코드가 존재하는지 확인합니다.
   *
   * @return 존재하면 true, 아니면 false
   */
  public boolean hasPreferredDong() {
    return preferredDongCode != null && !preferredDongCode.isBlank();
  }

  /**
   * 요청에서 선택한 콘셉트 목록을 반환합니다.
   *
   * @return 불변 콘셉트 Set
   */
  public Set<String> getSelectedConcepts() {
    return selectedConcepts;
  }

  /**
   * 선택된 콘셉트가 존재하는지 확인합니다.
   *
   * @return 콘셉트가 1개 이상이면 true, 아니면 false
   */
  public boolean hasSelectedConcepts() {
    return !selectedConcepts.isEmpty();
  }

  /**
   * 최근 30일 내 활동 이력이 존재하는지 반환합니다.
   *
   * @return 이력이 있으면 true, 없으면 false
   */
  public boolean hasHistory() {
    return hasHistory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RecommendationContext that)) {
      return false;
    }
    return personalizedConsent == that.personalizedConsent
        && hasHistory == that.hasHistory
        && Objects.equals(memberId, that.memberId)
        && Objects.equals(preferredDongCode, that.preferredDongCode)
        && Objects.equals(selectedConcepts, that.selectedConcepts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        memberId, personalizedConsent, preferredDongCode, selectedConcepts, hasHistory);
  }
}

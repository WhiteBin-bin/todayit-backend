package com.todayit.recommendation.rule;

import com.todayit.recommendation.model.CourseCandidate;
import com.todayit.recommendation.model.RecommendationContext;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

/** 개인화 추천 및 신호 누락 재정규화 규칙을 실행하는 Drools RuleUnitData 클래스입니다. */
public class RecommendationRuleUnit implements RuleUnitData {

  private final DataStore<RecommendationContext> context;
  private final DataStore<CourseCandidate> candidates;

  /** 기본 DataSource 저장소를 사용하는 RuleUnit 인스턴스를 생성합니다. */
  public RecommendationRuleUnit() {
    this(DataSource.createStore(), DataSource.createStore());
  }

  /**
   * 지정된 context와 candidates 저장소를 사용하는 RuleUnit 인스턴스를 생성합니다.
   *
   * @param context 사용자 요청 컨텍스트 DataStore
   * @param candidates 추천 후보 코스 DataStore
   */
  public RecommendationRuleUnit(
      DataStore<RecommendationContext> context, DataStore<CourseCandidate> candidates) {
    this.context = context;
    this.candidates = candidates;
  }

  /**
   * 사용자 요청 컨텍스트 DataStore를 반환합니다.
   *
   * @return context DataStore
   */
  public DataStore<RecommendationContext> getContext() {
    return context;
  }

  /**
   * 추천 후보 코스 DataStore를 반환합니다.
   *
   * @return candidates DataStore
   */
  public DataStore<CourseCandidate> getCandidates() {
    return candidates;
  }
}

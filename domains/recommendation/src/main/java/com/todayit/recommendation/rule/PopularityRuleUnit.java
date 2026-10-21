package com.todayit.recommendation.rule;

import com.todayit.recommendation.model.PopularityCandidate;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

/** 인기순 자격 검증 및 원점수 계산 규칙을 실행하는 Drools RuleUnitData 클래스입니다. */
public class PopularityRuleUnit implements RuleUnitData {

  private final DataStore<PopularityCandidate> candidates;

  /** 기본 DataSource 저장소를 사용하는 RuleUnit 인스턴스를 생성합니다. */
  public PopularityRuleUnit() {
    this(DataSource.createStore());
  }

  /**
   * 지정된 후보 저장소를 사용하는 RuleUnit 인스턴스를 생성합니다.
   *
   * @param candidates 인기순 평가 후보 DataStore
   */
  public PopularityRuleUnit(DataStore<PopularityCandidate> candidates) {
    this.candidates = candidates;
  }

  /**
   * 인기순 평가 후보 DataStore를 반환합니다.
   *
   * @return candidates DataStore
   */
  public DataStore<PopularityCandidate> getCandidates() {
    return candidates;
  }
}

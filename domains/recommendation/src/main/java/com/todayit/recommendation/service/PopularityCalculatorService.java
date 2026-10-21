package com.todayit.recommendation.service;

import com.todayit.recommendation.model.PopularityCandidate;
import com.todayit.recommendation.model.PopularityTargetType;
import com.todayit.recommendation.rule.PopularityRuleUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;

/**
 * Drools Rule Unit을 실행하여 인기 원점수를 계산하고 기획 정렬 규칙에 따라 순위를 산출하는 서비스입니다.
 *
 * <p>정렬 기준:
 *
 * <ol>
 *   <li>인기 원점수 내림차순 ({@code L×1 + S×2 + V×0.1})
 *   <li>코스 공개일 / 장소 최초 노출일 내림차순 (날짜 누락 {@code null}은 날짜가 있는 항목 뒤에 배치)
 *   <li>대상 ID 오름차순
 * </ol>
 */
public class PopularityCalculatorService {

  private static final Comparator<PopularityCandidate> FALLBACK_COMPARATOR =
      Comparator.comparing(
              PopularityCandidate::getExposedAt, Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(PopularityCandidate::getTargetId);

  private static final Comparator<PopularityCandidate> POPULARITY_RANK_COMPARATOR =
      Comparator.comparingDouble(PopularityCandidate::getPopularityScore)
          .reversed()
          .thenComparing(FALLBACK_COMPARATOR);

  /** 인기순 계산 서비스를 생성합니다. */
  public PopularityCalculatorService() {}

  /**
   * 후보 목록에 대해 Drools 인기순 규칙을 평가하고, 지정된 대상 유형의 유효 후보만 정렬하여 반환합니다.
   *
   * <p>코스와 장소는 서로 순위를 섞지 않으며, 비공개·임시저장·삭제 대상은 결과에서 제외됩니다.
   *
   * @param targetType 집계할 대상 유형(COURSE 또는 PLACE, null 불가)
   * @param candidates 평가할 후보 목록(null 또는 빈 목록이면 빈 결과 반환)
   * @return 인기 점수 및 보조 정렬 기준이 적용된 불변 목록
   * @throws NullPointerException targetType이 null인 경우
   */
  public List<PopularityCandidate> evaluateAndRank(
      PopularityTargetType targetType, List<PopularityCandidate> candidates) {
    Objects.requireNonNull(targetType, "대상 유형은 필수입니다.");
    if (candidates == null || candidates.isEmpty()) {
      return List.of();
    }

    PopularityRuleUnit unitData = new PopularityRuleUnit();
    for (PopularityCandidate candidate : candidates) {
      if (candidate != null) {
        unitData.getCandidates().add(candidate);
      }
    }

    try (RuleUnitInstance<PopularityRuleUnit> instance =
        RuleUnitProvider.get().createRuleUnitInstance(unitData)) {
      instance.fire();
    }

    return candidates.stream()
        .filter(Objects::nonNull)
        .filter(candidate -> candidate.getTargetType() == targetType)
        .filter(PopularityCandidate::isEligible)
        .sorted(POPULARITY_RANK_COMPARATOR)
        .toList();
  }

  /**
   * 정상 집계 데이터가 전혀 없을 때 공개일·최초 노출일 내림차순 및 ID 오름차순으로 대체 목록을 반환합니다.
   *
   * @param targetType 대상 유형(COURSE 또는 PLACE, null 불가)
   * @param candidates 후보 목록
   * @return 공개일/최초 노출일 기준 정렬된 유효 후보 불변 목록
   * @throws NullPointerException targetType이 null인 경우
   */
  public List<PopularityCandidate> rankByFallbackOrder(
      PopularityTargetType targetType, List<PopularityCandidate> candidates) {
    Objects.requireNonNull(targetType, "대상 유형은 필수입니다.");
    if (candidates == null || candidates.isEmpty()) {
      return List.of();
    }

    PopularityRuleUnit unitData = new PopularityRuleUnit();
    for (PopularityCandidate candidate : candidates) {
      if (candidate != null) {
        unitData.getCandidates().add(candidate);
      }
    }

    try (RuleUnitInstance<PopularityRuleUnit> instance =
        RuleUnitProvider.get().createRuleUnitInstance(unitData)) {
      instance.fire();
    }

    return candidates.stream()
        .filter(Objects::nonNull)
        .filter(candidate -> candidate.getTargetType() == targetType)
        .filter(PopularityCandidate::isEligible)
        .sorted(FALLBACK_COMPARATOR)
        .toList();
  }
}

package com.todayit.recommendation.service;

import com.todayit.recommendation.model.CourseCandidate;
import com.todayit.recommendation.model.RecommendationContext;
import com.todayit.recommendation.rule.RecommendationRuleUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;

/**
 * Drools Rule Unit을 실행하여 추천 코스 점수 계산 및 순위를 결정하는 서비스입니다.
 *
 * <p>정렬 기준:
 *
 * <ol>
 *   <li>최종 점수 내림차순
 *   <li>코스 공개일 내림차순 (null은 뒤로 배치)
 *   <li>코스 식별자(ID) 오름차순
 * </ol>
 */
public class RecommendationEngineService {

  private static final Comparator<CourseCandidate> FALLBACK_COMPARATOR =
      Comparator.comparing(
              CourseCandidate::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(CourseCandidate::getCourseId);

  private static final Comparator<CourseCandidate> RECOMMENDATION_RANK_COMPARATOR =
      Comparator.comparingDouble(CourseCandidate::getFinalScore)
          .reversed()
          .thenComparing(FALLBACK_COMPARATOR);

  /** 추천 엔진 서비스를 생성합니다. */
  public RecommendationEngineService() {}

  /**
   * 사용자 컨텍스트와 후보 코스 목록을 바탕으로 Drools 룰을 평가하고 정렬된 추천 목록을 반환합니다.
   *
   * @param context 사용자 요청 컨텍스트 (null 불가)
   * @param candidates 추천 후보 코스 목록 (최대 100개 권장, null이거나 빈 목록이면 빈 리스트 반환)
   * @return 최종 점수 및 보조 정렬 기준이 적용된 불변 후보 목록
   * @throws NullPointerException context가 null인 경우
   */
  public List<CourseCandidate> evaluateAndRank(
      RecommendationContext context, List<CourseCandidate> candidates) {
    Objects.requireNonNull(context, "추천 컨텍스트는 필수입니다.");
    if (candidates == null || candidates.isEmpty()) {
      return List.of();
    }

    double maxPopularityRawScore =
        candidates.stream()
            .filter(Objects::nonNull)
            .mapToDouble(CourseCandidate::getPopularityRawScore)
            .max()
            .orElse(0.0);

    for (CourseCandidate candidate : candidates) {
      if (candidate != null) {
        candidate.setFallbackToPopular(false);
        candidate.calculateConceptScore(context.getSelectedConcepts());
        candidate.normalizePopularityScore(maxPopularityRawScore);
      }
    }

    RecommendationRuleUnit unitData = new RecommendationRuleUnit();
    unitData.getContext().add(context);
    for (CourseCandidate candidate : candidates) {
      if (candidate != null) {
        unitData.getCandidates().add(candidate);
      }
    }

    try (RuleUnitInstance<RecommendationRuleUnit> instance =
        RuleUnitProvider.get().createRuleUnitInstance(unitData)) {
      instance.fire();
    }

    return candidates.stream()
        .filter(Objects::nonNull)
        .filter(CourseCandidate::isEligible)
        .sorted(RECOMMENDATION_RANK_COMPARATOR)
        .toList();
  }
}

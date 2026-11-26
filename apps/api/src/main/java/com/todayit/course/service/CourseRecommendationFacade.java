package com.todayit.course.service;

import com.todayit.common.exception.BusinessException;
import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.service.command.CourseRecommendationCommand;
import com.todayit.course.service.model.CoursePlaceItem;
import com.todayit.course.service.model.CoursePlaceRegion;
import com.todayit.course.service.model.CourseRecommendationProfile;
import com.todayit.course.service.model.RecommendedCourseResult;
import com.todayit.recommendation.model.CourseCandidate;
import com.todayit.recommendation.model.RecommendationContext;
import com.todayit.recommendation.service.RecommendationEngineService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 저장된 코스와 회원 활동을 기존 추천 엔진에 연결합니다. */
@Service
public class CourseRecommendationFacade {
  private static final Logger LOG = LoggerFactory.getLogger(CourseRecommendationFacade.class);
  private static final int HISTORY_WINDOW_DAYS = 30;
  private static final int POPULARITY_WINDOW_DAYS = 14;
  private static final double SCRAP_POPULARITY_WEIGHT = 2.0;

  private final CourseRecommendationQueryService recommendations;
  private final MemberCourseRecommendationQueryService members;
  private final PlaceCourseRegionQueryService regions;
  private final PlaceCourseQueryService places;
  private final RegionQueryService regionQueryService;
  private final RecommendationEngineService engine;
  private final Clock clock;

  /**
   * 추천 데이터 조회 서비스를 받습니다.
   *
   * @param recommendations 추천 데이터 조회 서비스
   * @param members 회원 선호 조회 서비스
   * @param regions 장소별 지역 조회 서비스
   * @param places 장소 조회 서비스
   * @param regionQueryService 코스 지역 요약 서비스
   */
  @Autowired
  public CourseRecommendationFacade(
      CourseRecommendationQueryService recommendations,
      MemberCourseRecommendationQueryService members,
      PlaceCourseRegionQueryService regions,
      PlaceCourseQueryService places,
      RegionQueryService regionQueryService) {
    this(
        recommendations,
        members,
        regions,
        places,
        regionQueryService,
        new RecommendationEngineService(),
        Clock.systemDefaultZone());
  }

  CourseRecommendationFacade(
      CourseRecommendationQueryService recommendations,
      MemberCourseRecommendationQueryService members,
      PlaceCourseRegionQueryService regions,
      PlaceCourseQueryService places,
      RegionQueryService regionQueryService,
      RecommendationEngineService engine,
      Clock clock) {
    this.recommendations = recommendations;
    this.members = members;
    this.regions = regions;
    this.places = places;
    this.regionQueryService = regionQueryService;
    this.engine = engine;
    this.clock = clock;
  }

  /**
   * 공개·확정 코스를 회원별 추천 순서로 반환합니다.
   *
   * <p>최근 활동은 30일, 인기는 14일을 사용합니다. 검증된 시간 저장 기능이 연결되기 전에는 시간 상한 요청의 모든 코스를 미확인으로 제외합니다.
   *
   * @param memberId 인증된 회원 식별자
   * @param command 검증된 필터
   * @return 추천 카드 목록, 결과가 없으면 빈 목록
   * @throws InvalidRequestException 등록되지 않은 콘셉트를 요청한 경우
   * @throws BusinessException 회원이 없거나 비활성 상태인 경우
   */
  @Transactional(readOnly = true)
  public List<RecommendedCourseResult> recommend(
      String memberId, CourseRecommendationCommand command) {
    CourseRecommendationProfile profile = members.getRecommendationProfile(memberId);
    recommendations.validateThemes(command.themes());
    if (command.durationMinutes() != null) {
      LOG.info("추천 코스 조회 완료: 예상시간 미확인으로 시간 필터 결과 없음");
      return List.of();
    }
    LocalDateTime now = LocalDateTime.now(clock);
    List<Course> candidates = recommendations.findCandidates();
    if (candidates.isEmpty()) {
      LOG.info("추천 코스 조회 완료: 공개 확정 코스 없음");
      return List.of();
    }
    List<Integer> placeIds =
        candidates.stream()
            .flatMap(c -> c.getCoursePlaces().stream())
            .map(CoursePlace::getPlaceId)
            .distinct()
            .toList();
    List<CoursePlaceRegion> placeRegions = regions.findPlaceRegions(placeIds);
    String preferredRegion =
        profile.personalizedConsent() && profile.sidoCode() != null
            ? profile.sidoCode() + (profile.sigunguCode() == null ? "" : profile.sigunguCode())
            : null;
    List<Set<String>> history =
        profile.personalizedConsent()
            ? recommendations
                .findRecentHistory(memberId, now.minusDays(HISTORY_WINDOW_DAYS), now)
                .stream()
                .map(this::themeNames)
                .filter(names -> !names.isEmpty())
                .toList()
            : List.of();
    Map<Integer, Long> recentLikes =
        counts(recommendations.countRecentLikes(now.minusDays(POPULARITY_WINDOW_DAYS), now));
    Map<Integer, Long> recentScraps =
        counts(recommendations.countRecentScraps(now.minusDays(POPULARITY_WINDOW_DAYS), now));
    List<CourseCandidate> facts = new ArrayList<>();
    Map<Integer, Course> byId = new HashMap<>();
    Set<Integer> matching = matchingPlaces(placeRegions, command.region());
    Set<Integer> preferred = matchingPlaces(placeRegions, preferredRegion);
    for (Course course : candidates) {
      Set<String> themes = themeNames(course);
      if (command.transport() != null && command.transport() != course.getTransport()) {
        continue;
      }
      if (!command.themes().isEmpty() && command.themes().stream().noneMatch(themes::contains)) {
        continue;
      }
      if (command.region() != null
          && course.getCoursePlaces().stream().noneMatch(p -> matching.contains(p.getPlaceId()))) {
        continue;
      }
      int matchingCount =
          (int)
              course.getCoursePlaces().stream()
                  .filter(p -> preferred.contains(p.getPlaceId()))
                  .count();
      CourseCandidate fact =
          new CourseCandidate(
              Integer.toString(course.getCourseId()),
              true,
              false,
              course.getCoursePlaces().size(),
              matchingCount,
              themes,
              recentLikes.getOrDefault(course.getCourseId(), 0L)
                  + SCRAP_POPULARITY_WEIGHT * recentScraps.getOrDefault(course.getCourseId(), 0L),
              course.getUpdatedAt() == null
                  ? null
                  : course.getUpdatedAt().atZone(clock.getZone()).toInstant());
      fact.setHistoryScore(similarity(themes, history));
      facts.add(fact);
      byId.put(course.getCourseId(), course);
    }
    RecommendationContext context =
        new RecommendationContext(
            memberId,
            profile.personalizedConsent(),
            preferredRegion,
            Set.copyOf(command.themes()),
            !history.isEmpty());
    List<CourseCandidate> ranked = engine.evaluateAndRank(context, facts);
    ranked =
        ranked.stream()
            .sorted(
                Comparator.comparingDouble(CourseCandidate::getFinalScore)
                    .reversed()
                    .thenComparing(
                        CourseCandidate::getPublishedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparingInt(c -> Integer.parseInt(c.getCourseId())))
            .toList();
    List<Integer> rankedIds = ranked.stream().map(c -> Integer.parseInt(c.getCourseId())).toList();
    if (rankedIds.isEmpty()) {
      LOG.info("추천 코스 조회 완료: 후보 {}건, 필터 적용 결과 없음", candidates.size());
      return List.of();
    }
    Map<Integer, Long> likeCounts = counts(recommendations.countLikes(rankedIds));
    Map<Integer, CoursePlaceItem> placeDetails = places.findPlacesByIds(placeIds);
    List<RecommendedCourseResult> results =
        rankedIds.stream().map(id -> toCard(byId.get(id), placeDetails, likeCounts)).toList();
    LOG.info("추천 코스 조회 완료: 후보 {}건, 결과 {}건", candidates.size(), results.size());
    return results;
  }

  private RecommendedCourseResult toCard(
      Course course, Map<Integer, CoursePlaceItem> details, Map<Integer, Long> likeCounts) {
    CoursePlaceItem first =
        course.getCoursePlaces().stream()
            .min(Comparator.comparingInt(CoursePlace::getOrder))
            .map(p -> details.get(p.getPlaceId()))
            .orElse(null);
    return new RecommendedCourseResult(
        course.getCourseId(),
        course.getTitle(),
        course.getCourseSummary(),
        first == null ? null : first.imageUrl(),
        regionQueryService.getCourseRegionSummary(
            course.getRegions().stream().map(r -> r.getRegionId()).toList()),
        themeNames(course).stream().sorted().toList(),
        course.getTransport(),
        course.getCoursePlaces().size(),
        likeCounts.getOrDefault(course.getCourseId(), 0L),
        course.getUpdatedAt());
  }

  private Set<String> themeNames(Course course) {
    return course.getThemes().stream().map(t -> t.getTheme().getName()).collect(Collectors.toSet());
  }

  private Set<Integer> matchingPlaces(List<CoursePlaceRegion> placeRegions, String region) {
    if (region == null) {
      return Set.of();
    }
    return placeRegions.stream()
        .filter(
            r ->
                r.sidoCode() != null
                    && region.startsWith(r.sidoCode())
                    && (region.length() == 2 || region.substring(2).equals(r.sigunguCode())))
        .map(CoursePlaceRegion::placeId)
        .collect(Collectors.toSet());
  }

  private double similarity(Set<String> themes, List<Set<String>> history) {
    if (themes.isEmpty() || history.isEmpty()) {
      return 0.0;
    }
    return history.stream()
        .mapToDouble(
            previous -> {
              Set<String> union = new HashSet<>(previous);
              union.addAll(themes);
              return (double) previous.stream().filter(themes::contains).count() / union.size();
            })
        .average()
        .orElse(0.0);
  }

  private Map<Integer, Long> counts(List<Object[]> rows) {
    return rows.stream()
        .collect(
            Collectors.toMap(
                row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));
  }
}

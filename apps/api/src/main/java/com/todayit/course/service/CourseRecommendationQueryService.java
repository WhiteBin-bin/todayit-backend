package com.todayit.course.service;

import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.Course;
import com.todayit.course.repository.CourseMemberLikeRepository;
import com.todayit.course.repository.CourseRecommendationRepository;
import com.todayit.course.repository.CourseRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 추천에 필요한 코스와 반응 데이터를 조회합니다. */
@Service
@Transactional(readOnly = true)
public class CourseRecommendationQueryService {

  private static final Logger LOG = LoggerFactory.getLogger(CourseRecommendationQueryService.class);

  private final CourseRecommendationRepository recommendations;
  private final CourseRepository courses;
  private final CourseMemberLikeRepository likes;

  /**
   * 코스와 반응 저장소를 받습니다.
   *
   * @param recommendations 추천 저장소
   * @param courses 코스 저장소
   * @param likes 좋아요 저장소
   */
  public CourseRecommendationQueryService(
      CourseRecommendationRepository recommendations,
      CourseRepository courses,
      CourseMemberLikeRepository likes) {
    this.recommendations = recommendations;
    this.courses = courses;
    this.likes = likes;
  }

  /**
   * 요청 콘셉트가 모두 등록되어 있는지 확인합니다.
   *
   * @param themes 중복 없는 콘셉트 이름
   * @throws InvalidRequestException 등록되지 않은 콘셉트가 포함된 경우
   */
  public void validateThemes(List<String> themes) {
    if (!themes.isEmpty() && courses.findThemesByNames(themes).size() != themes.size()) {
      throw new InvalidRequestException("지원하지 않는 코스 콘셉트입니다.");
    }
  }

  /**
   * 공개 확정 코스를 조회합니다.
   *
   * @return 추천 후보 목록
   */
  public List<Course> findCandidates() {
    List<Course> candidates = recommendations.findCandidates();
    LOG.info("추천 후보 공개 확정 코스 조회: candidateCount={}", candidates.size());
    return candidates;
  }

  /**
   * 최근 회원 활동 대상 코스를 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param since 집계 시작 시각, 포함
   * @param until 집계 종료 시각, 미포함
   * @return 활동 코스 목록
   */
  public List<Course> findRecentHistory(String memberId, LocalDateTime since, LocalDateTime until) {
    List<Course> history = recommendations.findRecentHistory(memberId, since, until);
    LOG.info("최근 회원 활동 코스 조회: historyCount={}", history.size());
    return history;
  }

  /**
   * 기간별 좋아요 수를 조회합니다.
   *
   * @param since 집계 시작 시각, 포함
   * @param until 집계 종료 시각, 미포함
   * @return 코스 식별자와 좋아요 수
   */
  public List<Object[]> countRecentLikes(LocalDateTime since, LocalDateTime until) {
    return recommendations.countRecentLikes(since, until);
  }

  /**
   * 기간별 스크랩 수를 조회합니다.
   *
   * @param since 집계 시작 시각, 포함
   * @param until 집계 종료 시각, 미포함
   * @return 코스 식별자와 스크랩 수
   */
  public List<Object[]> countRecentScraps(LocalDateTime since, LocalDateTime until) {
    return recommendations.countRecentScraps(since, until);
  }

  /**
   * 카드에 표시할 전체 좋아요 수를 일괄 조회합니다.
   *
   * @param courseIds 코스 식별자 목록
   * @return 코스 식별자와 좋아요 수
   */
  public List<Object[]> countLikes(List<Integer> courseIds) {
    return courseIds.isEmpty() ? List.of() : likes.countByCourseIds(courseIds);
  }
}

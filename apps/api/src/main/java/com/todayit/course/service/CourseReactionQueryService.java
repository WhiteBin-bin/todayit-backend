package com.todayit.course.service;

import com.todayit.course.repository.CourseMemberLikeRepository;
import com.todayit.course.repository.CourseScrapRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 코스 좋아요와 스크랩 조회를 담당합니다. */
@Service
@Transactional(readOnly = true)
public class CourseReactionQueryService {

  private static final Logger LOG = LoggerFactory.getLogger(CourseReactionQueryService.class);

  private final CourseMemberLikeRepository likes;
  private final CourseScrapRepository scraps;

  /**
   * 코스 반응 저장소를 받습니다.
   *
   * @param likes 좋아요 저장소
   * @param scraps 스크랩 저장소
   */
  public CourseReactionQueryService(
      CourseMemberLikeRepository likes, CourseScrapRepository scraps) {
    this.likes = likes;
    this.scraps = scraps;
  }

  /**
   * 카드에 표시할 코스별 좋아요 수를 일괄 조회합니다.
   *
   * @param courseIds 코스 식별자 목록
   * @return 코스 식별자와 좋아요 수 목록
   */
  public List<Object[]> countLikes(List<Integer> courseIds) {
    if (courseIds.isEmpty()) {
      return List.of();
    }
    List<Object[]> results = likes.countByCourseIds(courseIds);
    LOG.info(
        "코스별 좋아요 수 일괄 조회: courseCount={}, matchedCourseCount={}", courseIds.size(), results.size());
    return results;
  }

  /**
   * 회원이 코스에 좋아요를 등록했는지 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 좋아요 등록 여부
   */
  public boolean isLiked(String memberId, int courseId) {
    boolean liked = likes.existsByMemberIdAndCourseCourseId(memberId, courseId);
    LOG.info("코스 좋아요 여부 조회: courseId={}, liked={}", courseId, liked);
    return liked;
  }

  /**
   * 회원의 유효한 코스 스크랩 여부를 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 활성 상태이며 삭제되지 않은 스크랩 존재 여부
   */
  public boolean isScrapped(String memberId, int courseId) {
    boolean scrapped =
        scraps.existsByMemberIdAndCourseCourseIdAndActiveTrueAndDeletedFalse(memberId, courseId);
    LOG.info("코스 스크랩 여부 조회: courseId={}, scrapped={}", courseId, scrapped);
    return scrapped;
  }
}

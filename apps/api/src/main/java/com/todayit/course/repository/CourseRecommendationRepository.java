package com.todayit.course.repository;

import com.todayit.course.entity.Course;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** 추천 후보와 기간별 반응을 조회하는 코스 저장소입니다. */
public interface CourseRecommendationRepository extends Repository<Course, Integer> {
  /**
   * 공개·확정·미삭제 코스를 조회합니다.
   *
   * @return 추천 자격이 있는 코스 목록
   */
  @Query(
      "select c from Course c where c.status = com.todayit.course.entity.CourseStatus.ACTIVE "
          + "and c.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC and c.deletedAt is null")
  List<Course> findCandidates();

  /**
   * 기간 안에 등록된 코스별 좋아요 수를 조회합니다.
   *
   * @param since 집계 시작 시각, 포함
   * @param until 집계 종료 시각, 미포함
   * @return 코스 식별자와 좋아요 수
   */
  @Query(
      "select l.course.courseId, count(l) from CourseMemberLike l "
          + "where l.createdAt >= :since and l.createdAt < :until group by l.course.courseId")
  List<Object[]> countRecentLikes(
      @Param("since") LocalDateTime since, @Param("until") LocalDateTime until);

  /**
   * 기간 안에 등록된 유효 스크랩 수를 조회합니다.
   *
   * @param since 집계 시작 시각, 포함
   * @param until 집계 종료 시각, 미포함
   * @return 코스 식별자와 스크랩 수
   */
  @Query(
      "select s.course.courseId, count(s) from CourseScrap s "
          + "where s.active = true and s.deleted = false and s.createdAt >= :since "
          + "and s.createdAt < :until group by s.course.courseId")
  List<Object[]> countRecentScraps(
      @Param("since") LocalDateTime since, @Param("until") LocalDateTime until);

  /**
   * 회원이 기간 안에 좋아요 또는 스크랩한 코스를 중복 없이 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param since 활동 시작 시각, 포함
   * @param until 활동 종료 시각, 미포함
   * @return 삭제되지 않은 활동 대상 코스
   */
  @Query(
      "select c from Course c where c.deletedAt is null "
          + "and c.status = com.todayit.course.entity.CourseStatus.ACTIVE and ("
          + "exists (select l from CourseMemberLike l where l.course = c and l.memberId = :memberId "
          + "and l.createdAt >= :since and l.createdAt < :until) or "
          + "exists (select s from CourseScrap s where s.course = c and s.memberId = :memberId "
          + "and s.active = true and s.deleted = false and s.createdAt >= :since and s.createdAt < :until)) "
          + "and (c.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC or c.memberId = :memberId)")
  List<Course> findRecentHistory(
      @Param("memberId") String memberId,
      @Param("since") LocalDateTime since,
      @Param("until") LocalDateTime until);
}

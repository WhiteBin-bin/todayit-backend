package com.todayit.course.repository;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CourseScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 코스 스크랩 정보를 조회하고 저장하는 JPA Repository입니다. */
public interface CourseScrapRepository extends JpaRepository<CourseScrap, Integer> {

  /**
   * 회원이 코스를 활성 상태로 스크랩했는지 확인합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 활성 스크랩 존재 여부
   */
  boolean existsByMemberIdAndCourseCourseIdAndActiveTrueAndDeletedFalse(
      String memberId, int courseId);

  /**
   * 회원의 코스 스크랩을 생성하거나 기존 취소된 스크랩을 다시 활성화합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 반영된 스크랩 수
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value =
          "insert into course_scrap "
              + "(member_id, course_id, is_active, is_delete, created_at) "
              + "values (:memberId, :courseId, true, false, current_timestamp) "
              + "on conflict (member_id, course_id) do update set "
              + "is_active = true, is_delete = false, deleted_at = null, "
              + "updated_at = current_timestamp "
              + "where course_scrap.is_active = false or course_scrap.is_delete = true",
      nativeQuery = true)
  int upsertByMemberIdAndCourseId(
      @Param("memberId") String memberId, @Param("courseId") int courseId);

  /**
   * 회원의 활성 코스 스크랩을 취소합니다.
   *
   * @param memberId 회원 식별자
   * @param courseId 코스 식별자
   * @return 취소된 스크랩 수
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "update CourseScrap scrap "
          + "set scrap.active = false, scrap.deleted = true, "
          + "scrap.deletedAt = CURRENT_TIMESTAMP, scrap.updatedAt = CURRENT_TIMESTAMP "
          + "where scrap.memberId = :memberId "
          + "and scrap.course.courseId = :courseId "
          + "and scrap.active = true "
          + "and scrap.deleted = false")
  int cancelByMemberIdAndCourseId(
      @Param("memberId") String memberId, @Param("courseId") int courseId);

  /**
   * 코스의 활성 스크랩 수를 조회합니다.
   *
   * @param courseId 코스 식별자
   * @return 활성 스크랩 수
   */
  @Query(
      "select count(scrap) from CourseScrap scrap "
          + "where scrap.course.courseId = :courseId "
          + "and scrap.active = true "
          + "and scrap.deleted = false")
  long countActiveByCourseId(@Param("courseId") int courseId);

  /**
   * 회원이 활성 상태로 스크랩한 코스를 최신 스크랩 순서로 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param pageable 페이지 정보
   * @return 스크랩 코스 페이지
   */
  @Query(
      value =
          "select scrap.course from CourseScrap scrap "
              + "where scrap.memberId = :memberId "
              + "and scrap.active = true "
              + "and scrap.deleted = false "
              + "and scrap.course.status = com.todayit.course.entity.CourseStatus.ACTIVE "
              + "and scrap.course.deletedAt is null "
              + "and (scrap.course.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC "
              + "or scrap.course.memberId = :memberId) "
              + "order by coalesce(scrap.updatedAt, scrap.createdAt) desc, "
              + "scrap.course.courseId desc",
      countQuery =
          "select count(scrap) from CourseScrap scrap "
              + "where scrap.memberId = :memberId "
              + "and scrap.active = true "
              + "and scrap.deleted = false "
              + "and scrap.course.status = com.todayit.course.entity.CourseStatus.ACTIVE "
              + "and scrap.course.deletedAt is null "
              + "and (scrap.course.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC "
              + "or scrap.course.memberId = :memberId)")
  Page<Course> findScrappedCoursesLatest(@Param("memberId") String memberId, Pageable pageable);

  /**
   * 회원이 활성 상태로 스크랩한 코스를 오래된 스크랩 순서로 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param pageable 페이지 정보
   * @return 스크랩 코스 페이지
   */
  @Query(
      value =
          "select scrap.course from CourseScrap scrap "
              + "where scrap.memberId = :memberId "
              + "and scrap.active = true "
              + "and scrap.deleted = false "
              + "and scrap.course.status = com.todayit.course.entity.CourseStatus.ACTIVE "
              + "and scrap.course.deletedAt is null "
              + "and (scrap.course.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC "
              + "or scrap.course.memberId = :memberId) "
              + "order by coalesce(scrap.updatedAt, scrap.createdAt) asc, "
              + "scrap.course.courseId asc",
      countQuery =
          "select count(scrap) from CourseScrap scrap "
              + "where scrap.memberId = :memberId "
              + "and scrap.active = true "
              + "and scrap.deleted = false "
              + "and scrap.course.status = com.todayit.course.entity.CourseStatus.ACTIVE "
              + "and scrap.course.deletedAt is null "
              + "and (scrap.course.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC "
              + "or scrap.course.memberId = :memberId)")
  Page<Course> findScrappedCoursesOldest(@Param("memberId") String memberId, Pageable pageable);
}

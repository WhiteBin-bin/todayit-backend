package com.todayit.course.repository;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.Theme;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 코스 정보를 조회하고 저장하는 JPA Repository입니다. */
public interface CourseRepository extends JpaRepository<Course, Integer> {
  /**
   * 작성자의 장소 변경 요청을 직렬화하기 위해 작성자가 일치하는 코스 행만 잠그고 조회합니다.
   *
   * @param courseId 코스 식별자
   * @param memberId 인증된 작성자 식별자
   * @return 코스, 존재하지 않거나 작성자가 아니면 빈 Optional
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select course from Course course "
          + "where course.courseId = :courseId and course.memberId = :memberId")
  Optional<Course> findByIdAndMemberIdForUpdate(
      @Param("courseId") int courseId, @Param("memberId") String memberId);

  /**
   * 원본 코스와 포함된 장소 목록을 한 번에 즉시 적재하여 상태 확인과 장소 조회가 동일한 스냅샷을 사용하도록 조회합니다.
   *
   * @param courseId 코스 식별자
   * @return 장소가 함께 적재된 코스, 존재하지 않으면 빈 Optional
   */
  @Query(
      "select distinct course from Course course "
          + "left join fetch course.coursePlaces "
          + "where course.courseId = :courseId")
  Optional<Course> findByIdWithPlaces(@Param("courseId") int courseId);

  /**
   * 선택한 콘셉트를 조회합니다.
   *
   * @param names 콘셉트 이름 목록
   * @return 존재하는 콘셉트 목록
   */
  @Query("select theme from Theme theme where theme.name in :names")
  List<Theme> findThemesByNames(@Param("names") List<String> names);

  /**
   * 공개 여부와 관계없이 해당 장소가 포함된 활성 코스에서 0분을 제외한 체류 시간 평균을 조회합니다.
   *
   * @param placeId 장소 식별자
   * @return 평균 체류 시간(분), 0분을 제외한 데이터가 없으면 null
   */
  @Query(
      "select avg(cp.stayMinute) from CoursePlace cp "
          + "where cp.placeId = :placeId and cp.stayMinute <> 0 "
          + "and cp.course.status = com.todayit.course.entity.CourseStatus.ACTIVE")
  Double findAverageStayMinutes(@Param("placeId") int placeId);

  /**
   * 특정 장소를 포함한 공개 활성 코스를 페이지 단위로 조회합니다.
   *
   * @param placeId 장소 식별자
   * @param pageable 페이지와 정렬 조건
   * @return 장소를 포함한 코스 페이지
   */
  @Query(
      value =
          "select distinct course from Course course "
              + "join course.coursePlaces coursePlace "
              + "where coursePlace.placeId = :placeId "
              + "and course.status = com.todayit.course.entity.CourseStatus.ACTIVE "
              + "and course.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC",
      countQuery =
          "select count(distinct course) from Course course "
              + "join course.coursePlaces coursePlace "
              + "where coursePlace.placeId = :placeId "
              + "and course.status = com.todayit.course.entity.CourseStatus.ACTIVE "
              + "and course.visibility = com.todayit.course.entity.CourseVisibility.PUBLIC")
  Page<Course> findPublicActiveByPlaceId(@Param("placeId") int placeId, Pageable pageable);

  /**
   * 회원이 작성한 삭제되지 않은 코스를 상태 조건 및 페이징에 맞게 조회합니다.
   *
   * @param memberId 회원 식별자
   * @param status 코스 상태 필터, null이면 DELETED 제외 전체 조회
   * @param pageable 페이징 및 정렬 조건
   * @return 코스 페이지
   */
  @Query(
      value =
          "select course from Course course "
              + "where course.memberId = :memberId "
              + "and (:status is null and course.status <> com.todayit.course.entity.CourseStatus.DELETED "
              + "     or course.status = :status) "
              + "and course.deletedAt is null",
      countQuery =
          "select count(course) from Course course "
              + "where course.memberId = :memberId "
              + "and (:status is null and course.status <> com.todayit.course.entity.CourseStatus.DELETED "
              + "     or course.status = :status) "
              + "and course.deletedAt is null")
  Page<Course> findByMemberIdAndStatus(
      @Param("memberId") String memberId, @Param("status") CourseStatus status, Pageable pageable);

  /**
   * 코스의 공개 범위를 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param visibility 변경할 공개 범위
   * @param updatedAt 수정 시각
   * @return 변경된 행 수
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Course c
      set c.visibility = :visibility,
          c.updatedAt = :updatedAt
      where c.courseId = :courseId
      """)
  int updateVisibility(
      @Param("courseId") int courseId,
      @Param("visibility") CourseVisibility visibility,
      @Param("updatedAt") LocalDateTime updatedAt);

  /**
   * 코스의 상태를 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param status 변경할 상태
   * @param updatedAt 수정 시각
   * @return 변경된 행 수
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Course c
      set c.status = :status,
          c.updatedAt = :updatedAt
      where c.courseId = :courseId
      """)
  int updateStatus(
      @Param("courseId") int courseId,
      @Param("status") CourseStatus status,
      @Param("updatedAt") LocalDateTime updatedAt);

  /**
   * 코스 조회수를 1 증가시킵니다.
   *
   * @param courseId 코스 식별자
   * @return 변경된 행 수
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Course c
      set c.viewCount = c.viewCount + 1
      where c.courseId = :courseId
      """)
  int increaseViewCount(@Param("courseId") int courseId);

  /**
   * 임시저장 코스의 생성 조건을 수정합니다.
   *
   * @param courseId 코스 식별자
   * @param startAt 방문 시작 일시
   * @param endAt 방문 종료 일시
   * @param transport 이동수단
   * @param updatedAt 수정 시각
   * @return 변경된 행 수
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Course c
      set c.startAt = :startAt,
          c.endAt = :endAt,
          c.transport = :transport,
          c.updatedAt = :updatedAt
      where c.courseId = :courseId
      """)
  int updateConditions(
      @Param("courseId") int courseId,
      @Param("startAt") LocalDateTime startAt,
      @Param("endAt") LocalDateTime endAt,
      @Param("transport") CourseTransport transport,
      @Param("updatedAt") LocalDateTime updatedAt);

  /**
   * 코스의 이동수단을 변경합니다.
   *
   * @param courseId 코스 식별자
   * @param transport 변경할 이동수단
   * @param updatedAt 수정 시각
   * @return 변경된 행 수
   */
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      """
      update Course c
      set c.transport = :transport,
          c.updatedAt = :updatedAt
      where c.courseId = :courseId
      """)
  int updateTransport(
      @Param("courseId") int courseId,
      @Param("transport") CourseTransport transport,
      @Param("updatedAt") LocalDateTime updatedAt);
}

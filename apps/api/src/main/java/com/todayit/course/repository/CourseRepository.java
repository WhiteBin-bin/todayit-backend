package com.todayit.course.repository;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.Theme;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
}

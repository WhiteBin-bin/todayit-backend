package com.todayit.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.properties.hibernate.auto_quote_keyword=true"
    })
@EntityScan({"com.todayit.course.entity", "com.todayit.region.entity"})
@Import(CourseCreationRepositoryTest.RepositoryConfiguration.class)
class CourseRecommendationRepositoryTest {
  @Autowired private CourseRecommendationRepository recommendations;
  @Autowired private CourseRepository courses;
  @Autowired private EntityManager entities;
  @Autowired private JdbcTemplate jdbc;
  private final LocalDateTime now = LocalDateTime.of(2026, 11, 26, 12, 0);

  @Test
  @DisplayName("추천 후보 조회 시 비공개·임시저장(DRAFT)·삭제된 코스를 제외하고 공개 확정 코스만 반환한다")
  void excludesPrivateDraftAndDeletedCourses() {
    // given
    Course visible = save(CourseStatus.ACTIVE, CourseVisibility.PUBLIC);
    save(CourseStatus.DRAFT, CourseVisibility.PUBLIC);
    save(CourseStatus.ACTIVE, CourseVisibility.PRIVATE);
    Course deleted = save(CourseStatus.ACTIVE, CourseVisibility.PUBLIC);
    ReflectionTestUtils.setField(deleted, "deletedAt", now);
    entities.flush();
    entities.clear();

    // when
    List<Course> candidates = recommendations.findCandidates();

    // then
    assertThat(candidates).extracting(Course::getCourseId).containsExactly(visible.getCourseId());
  }

  @Test
  @DisplayName("최근 14일 인기 집계와 최근 30일 회원 활동 이력 조회 시 기간 경계를 정확히 반영한다")
  void countsWindowBoundariesAndReadsMemberHistory() {
    // given
    Course visible = save(CourseStatus.ACTIVE, CourseVisibility.PUBLIC);
    Course other = save(CourseStatus.ACTIVE, CourseVisibility.PUBLIC);
    like(visible, "me", now.minusDays(14));
    like(visible, "old", now.minusDays(14).minusSeconds(1));
    like(other, "future", now);

    // when
    List<Object[]> recentLikes = recommendations.countRecentLikes(now.minusDays(14), now);
    List<Course> recentHistory = recommendations.findRecentHistory("me", now.minusDays(30), now);

    // then
    assertThat(recentLikes)
        .satisfiesExactly(
            row -> {
              assertThat(((Number) row[0]).intValue()).isEqualTo(visible.getCourseId());
              assertThat(((Number) row[1]).longValue()).isEqualTo(1L);
            });
    assertThat(recentHistory)
        .extracting(Course::getCourseId)
        .containsExactly(visible.getCourseId());
  }

  private Course save(CourseStatus status, CourseVisibility visibility) {
    Course course =
        Course.createDraft("owner", "코스", CourseTransport.CAR, null, null, List.of(), now);
    ReflectionTestUtils.setField(course, "status", status);
    ReflectionTestUtils.setField(course, "visibility", visibility);
    return courses.saveAndFlush(course);
  }

  private void like(Course course, String member, LocalDateTime at) {
    jdbc.update(
        "insert into course_member_like (member_id, course_id, created_at) values (?, ?, ?)",
        member,
        course.getCourseId(),
        at);
  }
}

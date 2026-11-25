package com.todayit.course.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.course.entity.CourseMemberLike;
import com.todayit.course.entity.CourseScrap;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayName("코스 좋아요·스크랩 마이그레이션")
class CourseReactionMigrationTest {
  @Autowired private EntityManager entities;
  @Autowired private JdbcTemplate jdbc;

  private static final String MEMBER_ID = "course-reaction-test-member";
  private int courseId;

  @BeforeEach
  void setUp() {
    jdbc.update(
        "insert into member (member_id, nickname, provider) values (?, ?, 'GOOGLE')",
        MEMBER_ID,
        "migration-member");
    courseId =
        jdbc.queryForObject(
            """
        insert into course (member_id, title, transport, start_at, status, visibility)
        values (?, '테스트 코스', 'WALKING', CURRENT_TIMESTAMP, 'DRAFT', 'PRIVATE')
        returning course_id
        """,
            Integer.class,
            MEMBER_ID);
  }

  @Test
  @DisplayName("마이그레이션으로 생성한 좋아요 테이블을 엔티티로 조회한다")
  void readsLikeUsingEntityMapping() {
    // given
    int likeId =
        jdbc.queryForObject(
            "insert into course_member_like (member_id, course_id) values (?, ?) returning course_member_like_id",
            Integer.class,
            MEMBER_ID,
            courseId);

    // when
    CourseMemberLike like = entities.find(CourseMemberLike.class, likeId);

    // then
    assertThat(like).isNotNull();
    Map<String, Object> row =
        jdbc.queryForMap(
            "select * from course_member_like where course_member_like_id = ?", likeId);
    assertThat(row.get("member_id")).isEqualTo(MEMBER_ID);
    assertThat(row.get("course_id")).isEqualTo(courseId);
    assertThat(row.get("created_at")).isNotNull();
  }

  @Test
  @DisplayName("동일한 회원의 동일 코스 중복 좋아요는 UNIQUE 제약으로 거부한다")
  void rejectsDuplicateLike() {
    // given
    String sql = "insert into course_member_like (member_id, course_id) values (?, ?)";
    jdbc.update(sql, MEMBER_ID, courseId);

    // when
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class, () -> jdbc.update(sql, MEMBER_ID, courseId));

    // then
    assertThat(exception.getMostSpecificCause()).isInstanceOf(SQLException.class);
    SQLException cause = (SQLException) exception.getMostSpecificCause();
    assertThat(cause.getSQLState()).isEqualTo("23505");
  }

  @Test
  @DisplayName("스크랩 엔티티를 조회하고 미수정·미삭제 시각은 null로 보관한다")
  void readsScrapUsingEntityMapping() {
    // given
    int scrapId =
        jdbc.queryForObject(
            """
        insert into course_scrap (member_id, course_id, is_active, is_delete)
        values (?, ?, true, false) returning course_scrap_id
        """,
            Integer.class,
            MEMBER_ID,
            courseId);

    // when
    CourseScrap scrap = entities.find(CourseScrap.class, scrapId);

    // then
    assertThat(scrap).isNotNull();
    Map<String, Object> row =
        jdbc.queryForMap("select * from course_scrap where course_scrap_id = ?", scrapId);
    assertThat(row.get("member_id")).isEqualTo(MEMBER_ID);
    assertThat(row.get("course_id")).isEqualTo(courseId);
    assertThat(row.get("is_active")).isEqualTo(true);
    assertThat(row.get("is_delete")).isEqualTo(false);
    assertThat(row.get("created_at")).isNotNull();
    assertThat(row.get("updated_at")).isNull();
    assertThat(row.get("deleted_at")).isNull();
  }

  @Test
  @DisplayName("없는 회원의 코스 좋아요는 FK 제약으로 거부한다")
  void rejectsLikeWithMissingMember() {
    // given
    String sql = "insert into course_member_like (member_id, course_id) values (?, ?)";

    // when
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class,
            () -> jdbc.update(sql, "missing-member", courseId));

    // then
    assertThat(exception.getMostSpecificCause()).isInstanceOf(SQLException.class);
    SQLException cause = (SQLException) exception.getMostSpecificCause();
    assertThat(cause.getSQLState()).isEqualTo("23503");
  }

  @Test
  @DisplayName("없는 코스의 좋아요는 FK 제약으로 거부한다")
  void rejectsLikeWithMissingCourse() {
    // given
    String sql = "insert into course_member_like (member_id, course_id) values (?, ?)";

    // when
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class,
            () -> jdbc.update(sql, MEMBER_ID, Integer.MAX_VALUE));

    // then
    assertThat(exception.getMostSpecificCause()).isInstanceOf(SQLException.class);
    SQLException cause = (SQLException) exception.getMostSpecificCause();
    assertThat(cause.getSQLState()).isEqualTo("23503");
  }

  @Test
  @DisplayName("없는 회원의 코스 스크랩은 FK 제약으로 거부한다")
  void rejectsScrapWithMissingMember() {
    // given
    String sql =
        "insert into course_scrap (member_id, course_id, is_active, is_delete) values (?, ?, true, false)";

    // when
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class,
            () -> jdbc.update(sql, "missing-member", courseId));

    // then
    assertThat(exception.getMostSpecificCause()).isInstanceOf(SQLException.class);
    SQLException cause = (SQLException) exception.getMostSpecificCause();
    assertThat(cause.getSQLState()).isEqualTo("23503");
  }

  @Test
  @DisplayName("없는 코스의 스크랩은 FK 제약으로 거부한다")
  void rejectsScrapWithMissingCourse() {
    // given
    String sql =
        "insert into course_scrap (member_id, course_id, is_active, is_delete) values (?, ?, true, false)";

    // when
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class,
            () -> jdbc.update(sql, MEMBER_ID, Integer.MAX_VALUE));

    // then
    assertThat(exception.getMostSpecificCause()).isInstanceOf(SQLException.class);
    SQLException cause = (SQLException) exception.getMostSpecificCause();
    assertThat(cause.getSQLState()).isEqualTo("23503");
  }
}

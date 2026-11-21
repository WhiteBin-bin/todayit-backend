package com.todayit.course.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.Theme;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.properties.hibernate.auto_quote_keyword=true"
    })
@EntityScan("com.todayit.course.entity")
@Import(CourseCreationRepositoryTest.RepositoryConfiguration.class)
@DisplayName("코스 생성 저장소")
class CourseCreationRepositoryTest {
  @TestConfiguration(proxyBeanMethods = false)
  @EnableJpaRepositories(basePackageClasses = CourseRepository.class)
  static class RepositoryConfiguration {}

  @Autowired private CourseRepository courses;
  @Autowired private EntityManager entities;
  @Autowired private JdbcTemplate jdbc;

  @Test
  @DisplayName("종료 시각으로 생성한 DRAFT와 지역·콘셉트 관계를 함께 저장한다")
  void persistsDraftWithEndTime() {
    // given
    LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
    LocalDateTime end = start.plusHours(4);
    jdbc.update("insert into theme (theme_id, name) values (3, '감성적'), (4, '여유로움')");
    List<Theme> themes = courses.findThemesByNames(List.of("감성적", "여유로움"));
    Course course =
        Course.createDraft(
            "member-1", "종로 데이트", CourseTransport.WALKING, start, end, themes, start.minusDays(1));
    course.getRegions().add(com.todayit.course.entity.RegionCourse.create(course, 7));
    course.getRegions().add(com.todayit.course.entity.RegionCourse.create(course, 8));

    // when
    int id = courses.saveAndFlush(course).getSnapshot().courseId();
    entities.clear();

    // then
    Map<String, Object> row = jdbc.queryForMap("select * from course where course_id = ?", id);
    assertThat(row.get("MEMBER_ID")).isEqualTo("member-1");
    assertThat(row.get("STATUS")).isEqualTo("DRAFT");
    assertThat(row.get("VISIBILITY")).isEqualTo("PRIVATE");
    assertThat(row.get("VIEW_COUNT")).isEqualTo(0);
    assertThat(row.get("COURSE_SUMMARY")).isNull();
    assertThat(courses.findById(id).orElseThrow().getSnapshot().endAt()).isEqualTo(end);
    assertThat(
            jdbc.queryForList(
                "select region_id from region_course where course_id = ?", Integer.class, id))
        .containsExactlyInAnyOrder(7, 8);
    assertThat(
            jdbc.queryForList(
                "select theme_id from course_theme where course_id = ?", Integer.class, id))
        .containsExactlyInAnyOrder(3, 4);
    assertThat(
            jdbc.queryForObject(
                "select count(*) from course_place where course_id = ?", Integer.class, id))
        .isZero();
  }

  @Test
  @DisplayName("종료 시각 없이 생성한 DRAFT도 정상 저장된다")
  void persistsDraftWithoutEndTime() {
    // given
    LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
    jdbc.update("insert into theme (theme_id, name) values (3, '감성적'), (4, '여유로움')");
    List<Theme> themes = courses.findThemesByNames(List.of("감성적", "여유로움"));
    Course course =
        Course.createDraft(
            "member-1", "종로 데이트", CourseTransport.WALKING, start, null, themes, start.minusDays(1));
    course.getRegions().add(com.todayit.course.entity.RegionCourse.create(course, 7));
    course.getRegions().add(com.todayit.course.entity.RegionCourse.create(course, 8));

    // when
    int id = courses.saveAndFlush(course).getSnapshot().courseId();
    entities.clear();

    // then
    Map<String, Object> row = jdbc.queryForMap("select * from course where course_id = ?", id);
    assertThat(row.get("MEMBER_ID")).isEqualTo("member-1");
    assertThat(row.get("END_AT")).isNull();
    assertThat(courses.findById(id).orElseThrow().getSnapshot().endAt()).isNull();
  }

  @Test
  @DisplayName("코스 테마는 존재하지 않는 테마를 FK로 참조할 수 없다")
  void rejectsMissingThemeReference() {
    // given
    LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
    LocalDateTime end = start.plusHours(4);
    Course course =
        Course.createDraft(
            "member-1",
            "종로 데이트",
            CourseTransport.WALKING,
            start,
            end,
            List.of(entities.getReference(Theme.class, 999)),
            start);

    // when
    DataIntegrityViolationException exception =
        assertThrows(DataIntegrityViolationException.class, () -> courses.saveAndFlush(course));

    // then
    assertThat(exception.getCause()).isNotNull();
  }

  @Test
  @DisplayName("코스에 장소를 추가하고 저장하면 coursePlaceId가 정상 생성된다")
  void persistsCoursePlacesWithGeneratedId() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "종로 데이트",
            CourseTransport.WALKING,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    course = courses.saveAndFlush(course);

    // when
    addPlace(course, 10, null, 60);
    Course savedCourse = courses.saveAndFlush(course);
    CoursePlace savedPlace =
        savedCourse.getCoursePlaces().stream()
            .filter(cp -> cp.getOrder() == 1)
            .findFirst()
            .orElseThrow();

    // then
    assertThat(savedPlace.getCoursePlaceId()).isPositive();
    assertThat(savedPlace.getPlaceId()).isEqualTo(10);
    assertThat(savedPlace.getOrder()).isEqualTo(1);
    assertThat(savedPlace.getStayMinute()).isEqualTo(60);
  }

  @Test
  @DisplayName("동일한 장소를 2회까지 추가하여 저장할 수 있다")
  void persistsDuplicatePlacesUpToTwice() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "종로 데이트",
            CourseTransport.WALKING,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    course = courses.saveAndFlush(course);

    // when
    addPlace(course, 10, null, 60);
    addPlace(course, 10, null, 30);
    Course savedCourse = courses.saveAndFlush(course);

    // then
    List<CoursePlace> savedPlaces = savedCourse.getCoursePlaces();
    assertThat(savedPlaces).hasSize(2);
    assertThat(savedPlaces.get(0).getPlaceId()).isEqualTo(10);
    assertThat(savedPlaces.get(0).getOrder()).isEqualTo(1);
    assertThat(savedPlaces.get(0).getCoursePlaceId()).isPositive();
    assertThat(savedPlaces.get(1).getPlaceId()).isEqualTo(10);
    assertThat(savedPlaces.get(1).getOrder()).isEqualTo(2);
    assertThat(savedPlaces.get(1).getCoursePlaceId()).isPositive();
    assertThat(savedPlaces.get(0).getCoursePlaceId())
        .isNotEqualTo(savedPlaces.get(1).getCoursePlaceId());
  }

  @Test
  @DisplayName("코스 장소의 방문 순서를 변경하고 저장하면 DB 순서가 갱신된다")
  void reordersCoursePlacesAndPersists() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "종로 데이트",
            CourseTransport.WALKING,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    addPlace(course, 10, null, 60);
    addPlace(course, 20, null, 30);
    addPlace(course, 30, null, 45);
    course = courses.saveAndFlush(course);

    int id1 = course.getCoursePlaces().get(0).getCoursePlaceId();
    int id2 = course.getCoursePlaces().get(1).getCoursePlaceId();
    int id3 = course.getCoursePlaces().get(2).getCoursePlaceId();

    // when: [id3, id1, id2] 순서로 변경
    course.getCoursePlaces().get(2).setOrder(-1);
    course.getCoursePlaces().get(0).setOrder(-2);
    course.getCoursePlaces().get(1).setOrder(-3);
    courses.flush();
    course.getCoursePlaces().get(2).setOrder(1);
    course.getCoursePlaces().get(0).setOrder(2);
    course.getCoursePlaces().get(1).setOrder(3);
    Course savedCourse = courses.saveAndFlush(course);

    // then
    Map<Integer, Integer> orderMap =
        savedCourse.getCoursePlaces().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    CoursePlace::getCoursePlaceId, CoursePlace::getOrder));
    assertThat(orderMap.get(id3)).isEqualTo(1);
    assertThat(orderMap.get(id1)).isEqualTo(2);
    assertThat(orderMap.get(id2)).isEqualTo(3);
  }

  private CoursePlace addPlace(
      Course course, int placeId, Integer targetOrder, Integer stayMinute) {
    int maxOrder =
        course.getCoursePlaces().stream().mapToInt(CoursePlace::getOrder).max().orElse(0);
    int assignedOrder =
        (targetOrder == null || targetOrder > maxOrder) ? maxOrder + 1 : Math.max(1, targetOrder);
    CoursePlace cp = CoursePlace.create(course, placeId, assignedOrder, stayMinute);
    course.getCoursePlaces().add(cp);
    return cp;
  }
}

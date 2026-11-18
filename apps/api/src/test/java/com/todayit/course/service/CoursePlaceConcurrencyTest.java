package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("코스 장소 추가 동시성")
class CoursePlaceConcurrencyTest {
  @Autowired private CourseCommandService service;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private DataSource dataSource;

  private String memberId;
  private int courseId;
  private int firstPlaceId;
  private int secondPlaceId;

  @BeforeEach
  void setUp() {
    memberId = UUID.randomUUID().toString();
    jdbc.update(
        "insert into member (member_id, nickname, provider) values (?, ?, 'GOOGLE')",
        memberId,
        "concurrency-" + memberId);
    courseId =
        jdbc.queryForObject(
            """
        insert into course (member_id, title, transport, start_at, status, visibility)
        values (?, '동시 추가 테스트', 'WALKING', CURRENT_TIMESTAMP, 'DRAFT', 'PRIVATE')
        returning course_id
        """,
            Integer.class,
            memberId);
    firstPlaceId =
        jdbc.queryForObject(
            """
        insert into place (name, latitude, longitude, address, category, is_active, is_deleted)
        values ('첫 장소', 37.5, 127.0, '테스트 주소', 'CAFE_DESSERT', true, false)
        returning place_id
        """,
            Integer.class);
    secondPlaceId =
        jdbc.queryForObject(
            """
        insert into place (name, latitude, longitude, address, category, is_active, is_deleted)
        values ('다음 장소', 37.5, 127.0, '테스트 주소', 'CAFE_DESSERT', true, false)
        returning place_id
        """,
            Integer.class);
  }

  @AfterEach
  void tearDown() {
    jdbc.update("delete from course_place where course_id = ?", courseId);
    jdbc.update("delete from course where course_id = ?", courseId);
    jdbc.update("delete from place where place_id in (?, ?)", firstPlaceId, secondPlaceId);
    jdbc.update("delete from member where member_id = ?", memberId);
  }

  @Test
  @DisplayName("같은 코스에 동시에 장소를 추가해도 두 요청 모두 서로 다른 순서로 저장된다")
  void appendsPlacesWithoutOrderCollision() throws Exception {
    // given
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(2);
        Connection blocker = dataSource.getConnection()) {
      blocker.setAutoCommit(false);
      try (PreparedStatement lock =
          blocker.prepareStatement("select course_id from course where course_id = ? for update")) {
        lock.setInt(1, courseId);
        lock.executeQuery().close();
      }
      Future<AddCoursePlaceResult> first =
          executor.submit(
              () -> {
                ready.countDown();
                assertThat(start.await(15, TimeUnit.SECONDS)).as("동시 요청 시작 신호").isTrue();
                return service.addPlace(
                    memberId, new AddCoursePlaceCommand(courseId, firstPlaceId, null, 30));
              });
      Future<AddCoursePlaceResult> second =
          executor.submit(
              () -> {
                ready.countDown();
                assertThat(start.await(15, TimeUnit.SECONDS)).as("동시 요청 시작 신호").isTrue();
                return service.addPlace(
                    memberId, new AddCoursePlaceCommand(courseId, secondPlaceId, null, 30));
              });
      awaitReadyAndStart(ready, start, List.of(first, second));

      // when
      awaitBlockedRequests();
      blocker.commit();
      AddCoursePlaceResult firstResult = first.get(30, TimeUnit.SECONDS);
      AddCoursePlaceResult secondResult = second.get(30, TimeUnit.SECONDS);

      // then
      assertThat(List.of(firstResult.order(), secondResult.order()))
          .containsExactlyInAnyOrder(1, 2);
      assertThat(
              jdbc.queryForList(
                  "select \"order\" from course_place where course_id = ? order by \"order\"",
                  Integer.class,
                  courseId))
          .containsExactly(1, 2);
      assertThat(
              jdbc.queryForList(
                  "select place_id from course_place where course_id = ?", Integer.class, courseId))
          .containsExactlyInAnyOrder(firstPlaceId, secondPlaceId);
    }
  }

  @Test
  @DisplayName("같은 장소를 동시에 추가해도 두 번 제한을 지키고 초과 요청은 업무 오류로 거부한다")
  void preservesDuplicationLimitDuringConcurrentAdds() throws Exception {
    // given
    service.addPlace(memberId, new AddCoursePlaceCommand(courseId, firstPlaceId, null, 30));
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<AddCoursePlaceResult>> attempts = new ArrayList<>();
    try (ExecutorService executor = Executors.newFixedThreadPool(2);
        Connection blocker = dataSource.getConnection()) {
      blocker.setAutoCommit(false);
      try (PreparedStatement lock =
          blocker.prepareStatement("select course_id from course where course_id = ? for update")) {
        lock.setInt(1, courseId);
        lock.executeQuery().close();
      }
      for (int index = 0; index < 2; index++) {
        attempts.add(
            executor.submit(
                () -> {
                  ready.countDown();
                  assertThat(start.await(15, TimeUnit.SECONDS)).as("동시 요청 시작 신호").isTrue();
                  return service.addPlace(
                      memberId, new AddCoursePlaceCommand(courseId, firstPlaceId, null, 30));
                }));
      }
      awaitReadyAndStart(ready, start, attempts);

      // when
      awaitBlockedRequests();
      blocker.commit();
      List<AddCoursePlaceResult> successes = new ArrayList<>();
      List<Throwable> failures = new ArrayList<>();
      for (Future<AddCoursePlaceResult> attempt : attempts) {
        try {
          successes.add(attempt.get(30, TimeUnit.SECONDS));
        } catch (ExecutionException exception) {
          failures.add(exception.getCause());
        }
      }

      // then
      assertThat(successes).hasSize(1);
      assertThat(successes.get(0).order()).isEqualTo(2);
      assertThat(failures).hasSize(1);
      assertThat(failures.get(0)).isInstanceOf(MaxPlaceDuplicationExceededException.class);
      assertThat(
              jdbc.queryForObject(
                  "select count(*) from course_place where course_id = ? and place_id = ?",
                  Integer.class,
                  courseId,
                  firstPlaceId))
          .isEqualTo(2);
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  @DisplayName("타인의 장소 추가와 순서 변경은 작성자의 행 잠금을 기다리지 않고 거부된다")
  void rejectsNonWriterWithoutWaitingForOwnerLock(boolean reorder) throws Exception {
    try (ExecutorService executor = Executors.newSingleThreadExecutor();
        Connection blocker = dataSource.getConnection()) {
      blocker.setAutoCommit(false);
      try {
        try (PreparedStatement lock =
            blocker.prepareStatement(
                "select course_id from course where course_id = ? for update")) {
          lock.setInt(1, courseId);
          lock.executeQuery().close();
        }

        Future<?> request =
            executor.submit(
                () -> {
                  if (reorder) {
                    service.reorderPlaces(
                        "other-member", new UpdateCoursePlaceOrdersCommand(courseId, List.of(1)));
                  } else {
                    service.addPlace(
                        "other-member",
                        new AddCoursePlaceCommand(courseId, firstPlaceId, null, 30));
                  }
                });

        ExecutionException exception =
            assertThrows(ExecutionException.class, () -> request.get(5, TimeUnit.SECONDS));
        assertThat(exception.getCause()).isInstanceOf(CourseNotFoundException.class);
      } finally {
        blocker.rollback();
      }
    }
    assertThat(
            jdbc.queryForObject(
                "select count(*) from course_place where course_id = ?", Integer.class, courseId))
        .isZero();
  }

  @Test
  @DisplayName("준비 시간 초과 시 시작 대기 중인 작업을 취소하고 시작 신호를 보내지 않는다")
  void cancelsWaitingTaskWhenPreparationTimesOut() throws Exception {
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
      Future<?> waiting =
          executor.submit(
              () -> {
                ready.countDown();
                assertThat(start.await(15, TimeUnit.SECONDS)).isTrue();
                return null;
              });

      assertThrows(AssertionError.class, () -> awaitReadyAndStart(ready, start, List.of(waiting)));

      assertThat(waiting.isCancelled()).isTrue();
      assertThat(start.getCount()).isEqualTo(1);
    }
  }

  private void awaitReadyAndStart(
      CountDownLatch ready, CountDownLatch start, List<? extends Future<?>> attempts)
      throws InterruptedException {
    boolean started = false;
    try {
      assertThat(ready.await(10, TimeUnit.SECONDS)).as("동시 요청 준비 완료").isTrue();
      start.countDown();
      started = true;
    } finally {
      if (!started) {
        attempts.forEach(attempt -> attempt.cancel(true));
      }
    }
  }

  private void awaitBlockedRequests() throws InterruptedException {
    // 두 요청이 실제 DB 잠금에서 대기한 뒤 해제해야 동시 읽기 경쟁을 재현할 수 있습니다.
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    int blocked = 0;
    while (System.nanoTime() < deadline) {
      blocked =
          jdbc.queryForObject(
              """
          select count(*) from pg_stat_activity
          where datname = current_database() and cardinality(pg_blocking_pids(pid)) > 0
          """,
              Integer.class);
      if (blocked >= 2) {
        break;
      }
      Thread.sleep(20);
    }
    assertThat(blocked).isGreaterThanOrEqualTo(2);
  }
}

package com.todayit.course.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateCourseConditionsRequestTest {
  @Test
  @DisplayName("durationMinutes가 양수로 전달되면 endAt보다 우선 적용되어 검증을 통과한다")
  void acceptsDurationWithEarlierExplicitEndAt() {
    // given
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    UpdateCourseConditionsRequest request =
        new UpdateCourseConditionsRequest(null, startAt, startAt.minusHours(1), 60, null);

    // when
    Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(request::validate);

    // then
    assertDoesNotThrow(request::validate);
    assertThat(thrown).isNull();
  }

  @Test
  @DisplayName("durationMinutes 없이 endAt이 startAt보다 빠르면 INVALID_END_AT 예외를 던진다")
  void rejectsEarlierEndAtWithoutDuration() {
    // given
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    UpdateCourseConditionsRequest request =
        new UpdateCourseConditionsRequest(null, startAt, startAt.minusHours(1), null, null);

    // when
    CourseException exception = assertThrows(CourseException.class, request::validate);

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_END_AT);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  @DisplayName("durationMinutes가 0 이하이면 INVALID_END_AT 예외를 던진다")
  void rejectsNonPositiveDuration(int durationMinutes) {
    // given
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    UpdateCourseConditionsRequest request =
        new UpdateCourseConditionsRequest(
            null, startAt, startAt.plusHours(1), durationMinutes, null);

    // when
    CourseException exception = assertThrows(CourseException.class, request::validate);

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_END_AT);
  }
}

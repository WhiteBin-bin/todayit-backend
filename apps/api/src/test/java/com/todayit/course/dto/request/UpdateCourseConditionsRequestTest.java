package com.todayit.course.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateCourseConditionsRequestTest {
  @Test
  void acceptsDurationWithEarlierExplicitEndAt() {
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    UpdateCourseConditionsRequest request =
        new UpdateCourseConditionsRequest(null, startAt, startAt.minusHours(1), 60, null);

    assertThatCode(request::validate).doesNotThrowAnyException();
  }

  @Test
  void rejectsEarlierEndAtWithoutDuration() {
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    UpdateCourseConditionsRequest request =
        new UpdateCourseConditionsRequest(null, startAt, startAt.minusHours(1), null, null);

    CourseException exception = assertThrows(CourseException.class, request::validate);

    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_END_AT);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void rejectsNonPositiveDuration(int durationMinutes) {
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    UpdateCourseConditionsRequest request =
        new UpdateCourseConditionsRequest(
            null, startAt, startAt.plusHours(1), durationMinutes, null);

    CourseException exception = assertThrows(CourseException.class, request::validate);

    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_END_AT);
  }
}

package com.todayit.course.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseTest {

  @Test
  @DisplayName("입력 조건으로 비공개 임시저장 코스를 정상 생성한다")
  void createsDraftSuccessfully() {
    // given
    LocalDateTime now = LocalDateTime.of(2026, 11, 24, 10, 0);

    // when
    Course course =
        Course.createDraft(
            "member-1",
            "내 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 11, 24, 10, 0),
            LocalDateTime.of(2026, 11, 24, 18, 0),
            List.of(),
            now);

    // then
    assertThat(course.getMemberId()).isEqualTo("member-1");
    assertThat(course.getTitle()).isEqualTo("내 코스");
    assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
    assertThat(course.getVisibility()).isEqualTo(CourseVisibility.PRIVATE);
    assertThat(course.getCreatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("원본 코스로부터 새로운 임시 코스를 복사 생성한다")
  void createsCopyDraftSuccessfully() {
    // given
    Course source =
        Course.createDraft(
            "owner-member",
            "원본 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 11, 24, 10, 0),
            LocalDateTime.of(2026, 11, 24, 18, 0),
            List.of(),
            LocalDateTime.of(2026, 11, 24, 9, 0));
    LocalDateTime now = LocalDateTime.of(2026, 11, 24, 12, 0);

    // when
    Course copied = Course.createCopyDraft("new-member", source, now);

    // then
    assertThat(copied.getMemberId()).isEqualTo("new-member");
    assertThat(copied.getTitle()).isNull();
    assertThat(copied.getStatus()).isEqualTo(CourseStatus.DRAFT);
    assertThat(copied.getVisibility()).isEqualTo(CourseVisibility.PRIVATE);
    assertThat(copied.getCreatedAt()).isEqualTo(now);
  }
}

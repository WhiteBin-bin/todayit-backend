package com.todayit.example.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.todayit.example.course.controller.CourseController;
import com.todayit.example.course.dto.request.CreateCourseRequest;
import com.todayit.example.course.dto.response.CourseResponse;
import com.todayit.example.course.entity.Course;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CourseCommandServiceTest {

  @Test
  void createsAndReturnsStoredCourse() {
    AtomicReference<Course> stored = new AtomicReference<>();
    CourseCommandService service =
        new CourseCommandService(
            course -> {
              stored.set(course);
              return course;
            },
            () -> "course-1");
    CourseController controller =
        new CourseController(new CourseFacade(memberId -> memberId.equals("member-1"), service));

    CourseResponse response = controller.create("member-1", new CreateCourseRequest("주말 산책"));

    assertThat(response).isEqualTo(new CourseResponse("course-1", "주말 산책"));
    assertThat(stored.get().getName()).isEqualTo("주말 산책");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t"})
  void rejectsMissingNameAtRequestBoundary(String name) {
    assertThatIllegalArgumentException().isThrownBy(() -> new CreateCourseRequest(name).validate());
  }

  @Test
  void doesNotSaveWhenMemberDoesNotExist() {
    AtomicReference<Course> stored = new AtomicReference<>();
    CourseCommandService service =
        new CourseCommandService(
            course -> {
              stored.set(course);
              return course;
            },
            () -> "course-1");
    CourseController controller =
        new CourseController(new CourseFacade(memberId -> false, service));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> controller.create("missing-member", new CreateCourseRequest("주말 산책")));
    assertThat(stored.get()).isNull();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t"})
  void rejectsMissingNameWithoutHttp(String name) {
    AtomicReference<Course> stored = new AtomicReference<>();
    CourseCommandService service =
        new CourseCommandService(
            course -> {
              stored.set(course);
              return course;
            },
            () -> "course-1");

    assertThatIllegalArgumentException().isThrownBy(() -> service.create(name));
    assertThat(stored.get()).isNull();
  }
}

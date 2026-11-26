package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todayit.common.pagination.PageResult;
import com.todayit.course.entity.Course;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.repository.CourseRepository;
import com.todayit.course.service.model.CourseResult;
import com.todayit.course.service.model.CourseSort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** 장소별 코스 조회 업무를 검증합니다. */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseQueryService 단위 테스트")
class CourseQueryServiceTest {

  @Mock private CourseRepository courseRepository;

  @Mock private Course course;

  @Test
  @DisplayName("체류 시간 데이터가 없으면 평균 체류 시간을 0분으로 반환한다")
  void returnsZeroWhenNoStayMinutesExist() {
    // given
    when(courseRepository.findAverageStayMinutes(1)).thenReturn(null);

    // when
    long result = new CourseQueryService(courseRepository).getAverageStayMinutes(1);

    // then
    assertThat(result).isZero();
  }

  @Test
  @DisplayName("평균 체류 시간을 분 단위 정수로 반올림한다")
  void roundsAverageStayMinutes() {
    // given
    when(courseRepository.findAverageStayMinutes(1)).thenReturn(35.5);

    // when
    long result = new CourseQueryService(courseRepository).getAverageStayMinutes(1);

    // then
    assertThat(result).isEqualTo(36);
  }

  @Test
  @DisplayName("장소를 포함한 코스를 최신순으로 페이지 조회한다")
  void findsLatestCoursesByPlace() {
    // given
    Sort expectedSort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("courseId"));
    when(course.getSnapshot())
        .thenReturn(
            new Course.CourseSnapshot(
                1,
                "종로 맛집과 카페 코스",
                "종로 코스",
                CourseTransport.WALKING,
                LocalDateTime.of(2026, 10, 3, 11, 0),
                LocalDateTime.of(2026, 10, 3, 15, 0),
                25));
    when(courseRepository.findPublicActiveByPlaceId(1, PageRequest.of(1, 5, expectedSort)))
        .thenReturn(new PageImpl<>(List.of(course), PageRequest.of(1, 5, expectedSort), 6));
    CourseQueryService service = new CourseQueryService(courseRepository);

    // when
    PageResult<CourseResult> result = service.findCoursesByPlace(1, 1, 5, CourseSort.LATEST);

    // then
    assertThat(result.content()).hasSize(1);
    assertThat(result.content().getFirst().title()).isEqualTo("종로 맛집과 카페 코스");
    assertThat(result.page()).isEqualTo(1);
    assertThat(result.size()).isEqualTo(5);
    assertThat(result.totalElements()).isEqualTo(6);
    verify(courseRepository).findPublicActiveByPlaceId(1, PageRequest.of(1, 5, expectedSort));
  }

  @Test
  @DisplayName("장소를 포함한 코스를 인기순으로 조회한다")
  void findsPopularCoursesByPlace() {
    // given
    Sort expectedSort = Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("courseId"));
    when(courseRepository.findPublicActiveByPlaceId(
            eq(1),
            argThat(pageable -> pageable != null && pageable.getSort().equals(expectedSort))))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
    CourseQueryService service = new CourseQueryService(courseRepository);

    // when
    PageResult<CourseResult> result = service.findCoursesByPlace(1, 0, 20, CourseSort.POPULAR);

    // then
    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
  }

  @Test
  @DisplayName("존재하는 활성 코스를 정상 조회한다")
  void getsCourseSuccessfully() {
    // given
    when(course.getStatus()).thenReturn(CourseStatus.ACTIVE);
    when(course.getDeletedAt()).thenReturn(null);
    when(courseRepository.findById(1)).thenReturn(Optional.of(course));
    CourseQueryService service = new CourseQueryService(courseRepository);

    // when
    Course result = service.getCourse(1);

    // then
    assertThat(result).isSameAs(course);
  }

  @Test
  @DisplayName("존재하지 않는 코스는 예외를 던진다")
  void throwsNotFoundWhenCourseDoesNotExist() {
    // given
    when(courseRepository.findById(99)).thenReturn(Optional.empty());
    CourseQueryService service = new CourseQueryService(courseRepository);

    // when
    CourseException exception = assertThrows(CourseException.class, () -> service.getCourse(99));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("삭제된 코스는 조회 시 예외를 던진다")
  void throwsNotFoundWhenCourseIsDeleted() {
    // given
    when(course.getStatus()).thenReturn(CourseStatus.DELETED);
    when(courseRepository.findById(1)).thenReturn(Optional.of(course));
    CourseQueryService service = new CourseQueryService(courseRepository);

    // when
    CourseException exception = assertThrows(CourseException.class, () -> service.getCourse(1));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("회원의 코스 목록 조회를 CourseRepository에 정상적으로 위임한다")
  void delegatesFindMyCoursesToRepository() {
    // given
    CourseQueryService service = new CourseQueryService(courseRepository);
    PageRequest pageRequest = PageRequest.of(0, 20);
    Page<Course> expectedPage = new PageImpl<>(List.of(course), pageRequest, 1);
    when(courseRepository.findByMemberIdAndStatus("member-1", CourseStatus.ACTIVE, pageRequest))
        .thenReturn(expectedPage);

    // when
    Page<Course> result = service.findMyCourses("member-1", CourseStatus.ACTIVE, pageRequest);

    // then
    assertThat(result).isEqualTo(expectedPage);
    verify(courseRepository).findByMemberIdAndStatus("member-1", CourseStatus.ACTIVE, pageRequest);
  }
}

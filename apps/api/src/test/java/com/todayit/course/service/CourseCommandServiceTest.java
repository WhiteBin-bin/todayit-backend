package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.Theme;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.CoursePlaceNotFoundException;
import com.todayit.course.exception.InvalidCoursePlaceOrderException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.repository.CourseRepository;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceEntities;
import com.todayit.course.service.model.CreateCourseResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("코스 생성 및 장소 추가 서비스")
class CourseCommandServiceTest {
  @Mock private CourseRepository courses;
  @InjectMocks private CourseCommandService service;

  @Test
  @DisplayName("코스를 생성하면 저장된 DRAFT의 courseId를 반환한다")
  void createsDraftWithoutGeneratedContent() {
    // given
    when(courses.findThemesByNames(List.of("감성적"))).thenReturn(List.of(mock(Theme.class)));
    when(courses.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    CreateCourseResult result = service.createCourse("member-1", command());

    // then
    assertThat(result.courseId()).isEqualTo(0);
  }

  @Test
  @DisplayName("지원하지 않는 콘셉트는 저장 전에 거부한다")
  void rejectsUnknownTheme() {
    // given
    when(courses.findThemesByNames(List.of("감성적"))).thenReturn(List.of());

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.createCourse("member-1", command()));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.UNSUPPORTED_THEME);
    verify(courses, never()).save(any());
  }

  @Test
  @DisplayName("코스에 장소를 추가하면 저장 후 생성된 장소 정보를 반환한다")
  void addsPlaceSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, null, 60);
    AddCoursePlaceEntities result = service.addPlace("member-1", command);

    // then
    assertThat(result.addedPlace().getPlaceId()).isEqualTo(10);
    assertThat(result.addedPlace().getOrder()).isEqualTo(1);
    assertThat(result.addedPlace().getStayMinute()).isEqualTo(60);
    assertThat(result.places()).hasSize(1);
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("동일한 장소를 2회까지 추가하는 것은 허용된다")
  void allowsAddingSamePlaceUpToTwice() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    addPlace(course, 10, null, 60);
    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, null, 45);
    AddCoursePlaceEntities result = service.addPlace("member-1", command);

    // then
    assertThat(result.addedPlace().getPlaceId()).isEqualTo(10);
    assertThat(result.addedPlace().getOrder()).isEqualTo(2);
    assertThat(result.addedPlace().getStayMinute()).isEqualTo(45);
    assertThat(result.places()).hasSize(2);
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("동일한 장소를 3회 추가하려고 하면 예외가 발생한다")
  void rejectsAddingSamePlaceMoreThanTwice() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    addPlace(course, 10, null, 60);
    addPlace(course, 10, null, 45);
    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));

    // when & then
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, null, 30);
    assertThrows(
        MaxPlaceDuplicationExceededException.class, () -> service.addPlace("member-1", command));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("코스가 존재하지 않으면 장소 추가 시 404 예외를 던진다")
  void rejectsAddingPlaceWhenCourseNotFound() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(999, "member-1")).thenReturn(Optional.empty());

    // when & then
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(999, 10, null, 60);
    assertThrows(CourseNotFoundException.class, () -> service.addPlace("member-1", command));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("작성자가 아니면 장소 추가 시 404 예외를 던진다")
  void rejectsAddingPlaceWhenNotWriter() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(1, "member-2")).thenReturn(Optional.empty());

    // when & then
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, null, 60);
    assertThrows(CourseNotFoundException.class, () -> service.addPlace("member-2", command));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("코스 중간에 장소를 추가하면 이후 장소들의 순서가 1씩 밀린다")
  void addsPlaceAtSpecificOrderAndShiftsExistingPlaces() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    addPlace(course, 10, null, 60);
    addPlace(course, 20, null, 45);
    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 30, 2, 30);
    AddCoursePlaceEntities result = service.addPlace("member-1", command);

    // then
    assertThat(result.addedPlace().getPlaceId()).isEqualTo(30);
    assertThat(result.addedPlace().getOrder()).isEqualTo(2);
    assertThat(result.addedPlace().getStayMinute()).isEqualTo(30);
    assertThat(result.places()).hasSize(3);
    assertThat(course.getCoursePlaces().get(1).getOrder()).isEqualTo(3);
    verify(courses).flush();
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("모든 코스 장소 식별자가 유효하면 순서가 일괄 변경된다")
  void updatesPlaceOrdersSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    CoursePlace p1 = addPlace(course, 10, null, 60);
    CoursePlace p2 = addPlace(course, 20, null, 45);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);
    ReflectionTestUtils.setField(p2, "coursePlaceId", 102);

    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    UpdateCoursePlaceOrdersCommand command =
        new UpdateCoursePlaceOrdersCommand(1, List.of(102, 101));
    List<CoursePlace> result = service.updatePlaceOrders("member-1", command);

    // then
    assertThat(result).hasSize(2);
    assertThat(p2.getOrder()).isEqualTo(1);
    assertThat(p1.getOrder()).isEqualTo(2);
    verify(courses).flush();
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("순서 변경 요청에 코스 장소가 누락되거나 다른 장소가 포함되면 예외를 던진다")
  void rejectsUpdatePlaceOrdersWhenIdsMismatch() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    CoursePlace p1 = addPlace(course, 10, null, 60);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);

    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));

    // when & then
    UpdateCoursePlaceOrdersCommand command =
        new UpdateCoursePlaceOrdersCommand(1, List.of(101, 999));
    assertThrows(
        InvalidCoursePlaceOrderException.class,
        () -> service.updatePlaceOrders("member-1", command));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("중간 순서의 장소를 삭제하면 뒤에 위치한 장소들의 순서가 1씩 앞당겨진다")
  void removesMiddlePlaceAndShiftsSubsequentOrders() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    CoursePlace p1 = addPlace(course, 10, null, 60);
    CoursePlace p2 = addPlace(course, 20, null, 45);
    CoursePlace p3 = addPlace(course, 30, null, 30);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);
    ReflectionTestUtils.setField(p2, "coursePlaceId", 102);
    ReflectionTestUtils.setField(p3, "coursePlaceId", 103);

    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    List<CoursePlace> remainingPlaces = service.removePlace("member-1", 1, 102);

    // then
    assertThat(remainingPlaces).hasSize(2);
    assertThat(p1.getOrder()).isEqualTo(1);
    assertThat(p3.getOrder()).isEqualTo(2);
    verify(courses).flush();
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("마지막 순서의 장소를 삭제하면 다른 장소의 순서 변경 없이 해당 장소만 삭제된다")
  void removesLastPlaceWithoutShiftingOrders() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    CoursePlace p1 = addPlace(course, 10, null, 60);
    CoursePlace p2 = addPlace(course, 20, null, 45);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);
    ReflectionTestUtils.setField(p2, "coursePlaceId", 102);

    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    List<CoursePlace> remainingPlaces = service.removePlace("member-1", 1, 102);

    // then
    assertThat(remainingPlaces).hasSize(1);
    assertThat(p1.getOrder()).isEqualTo(1);
    verify(courses, never()).flush();
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("코스의 유일한 장소를 삭제해도 코스는 삭제되지 않고 빈 DRAFT 상태로 유지된다")
  void keepsEmptyDraftCourseWhenRemovingOnlyPlace() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    CoursePlace p1 = addPlace(course, 10, null, 60);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);

    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));
    when(courses.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    List<CoursePlace> remainingPlaces = service.removePlace("member-1", 1, 101);

    // then
    assertThat(remainingPlaces).isEmpty();
    assertThat(course.getCoursePlaces()).isEmpty();
    assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
    verify(courses).saveAndFlush(course);
  }

  @Test
  @DisplayName("장소 삭제 시 코스가 존재하지 않으면 404 예외를 던진다")
  void rejectsRemovePlaceWhenCourseNotFound() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(999, "member-1")).thenReturn(Optional.empty());

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.removePlace("member-1", 999, 101));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("장소 삭제 시 작성자가 아니면 404 예외를 던진다")
  void rejectsRemovePlaceWhenNotWriter() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(1, "member-2")).thenReturn(Optional.empty());

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.removePlace("member-2", 1, 101));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("코스 내에 존재하지 않는 장소 식별자 삭제 요청 시 404 예외를 던진다")
  void rejectsRemovePlaceWhenPlaceNotFoundInCourse() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    CoursePlace p1 = addPlace(course, 10, null, 60);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);

    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));

    // when & then
    assertThrows(CoursePlaceNotFoundException.class, () -> service.removePlace("member-1", 1, 999));
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("코스 삭제 시 작성자의 코스이면 DB에서 영구 물리 삭제한다")
  void deletesCoursePermanentlyWhenWriter() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "삭제할 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));

    // when
    service.deleteCourse("member-1", 1);

    // then
    verify(courses).delete(course);
    verify(courses).flush();
  }

  @Test
  @DisplayName("코스 삭제 시 코스가 존재하지 않으면 404 예외를 던진다")
  void rejectsDeleteCourseWhenNotFound() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(999, "member-1")).thenReturn(Optional.empty());

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.deleteCourse("member-1", 999));
    verify(courses, never()).delete(any());
  }

  @Test
  @DisplayName("코스 삭제 시 작성자가 아니면 404 예외를 던진다")
  void rejectsDeleteCourseWhenNotWriter() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(1, "member-2")).thenReturn(Optional.empty());

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.deleteCourse("member-2", 1));
    verify(courses, never()).delete(any());
  }

  @Test
  @DisplayName("코스 삭제 시 이미 탈퇴/제재로 삭제된 코스이면 404 예외를 던진다")
  void rejectsDeleteCourseWhenAlreadyDeleted() {
    // given
    Course course =
        Course.createDraft(
            "member-1",
            "삭제된 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "status", CourseStatus.DELETED);
    when(courses.findByIdAndMemberIdForUpdate(1, "member-1")).thenReturn(Optional.of(course));

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.deleteCourse("member-1", 1));
    verify(courses, never()).delete(any());
  }

  @Test
  @DisplayName("공개 확정 코스를 복사하면 장소 순서는 유지되고 체류시간/이동수단 등은 초기화된 새 DRAFT를 생성한다")
  void copiesPublicActiveCourseSuccessfully() {
    // given
    Course sourceCourse =
        Course.createDraft(
            "member-1",
            "원본 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            LocalDateTime.of(2026, 12, 1, 14, 0),
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(sourceCourse, "courseId", 1);
    ReflectionTestUtils.setField(sourceCourse, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(sourceCourse, "visibility", CourseVisibility.PUBLIC);

    CoursePlace p1 = addPlace(sourceCourse, 10, 1, 60);
    CoursePlace p2 = addPlace(sourceCourse, 20, 2, 45);
    ReflectionTestUtils.setField(p1, "coursePlaceId", 101);
    ReflectionTestUtils.setField(p2, "coursePlaceId", 102);

    when(courses.findByIdWithPlaces(1)).thenReturn(Optional.of(sourceCourse));
    when(courses.save(any(Course.class)))
        .thenAnswer(
            invocation -> {
              Course saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "courseId", 2);
              return saved;
            });

    // when
    Course result = service.copyCourse("member-2", 1);

    // then
    assertThat(result.getCourseId()).isEqualTo(2);

    ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
    verify(courses).save(captor.capture());
    Course copied = captor.getValue();

    assertThat(copied.getMemberId()).isEqualTo("member-2");
    assertThat(copied.getStatus()).isEqualTo(CourseStatus.DRAFT);
    assertThat(copied.getVisibility()).isEqualTo(CourseVisibility.PRIVATE);
    assertThat(copied.getTitle()).isNull();
    assertThat(copied.getTransport()).isNull();
    assertThat(copied.getStartAt()).isNull();
    assertThat(copied.getEndAt()).isNull();
    assertThat(copied.getCoursePlaces()).hasSize(2);
    assertThat(copied.getCoursePlaces().get(0).getPlaceId()).isEqualTo(10);
    assertThat(copied.getCoursePlaces().get(0).getOrder()).isEqualTo(1);
    assertThat(copied.getCoursePlaces().get(0).getStayMinute()).isNull();
    assertThat(copied.getCoursePlaces().get(1).getPlaceId()).isEqualTo(20);
    assertThat(copied.getCoursePlaces().get(1).getOrder()).isEqualTo(2);
    assertThat(copied.getCoursePlaces().get(1).getStayMinute()).isNull();
  }

  @Test
  @DisplayName("본인의 비공개 확정 코스는 복사할 수 있다")
  void copiesOwnPrivateActiveCourseSuccessfully() {
    // given
    Course sourceCourse =
        Course.createDraft(
            "member-1",
            "본인 비공개 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(sourceCourse, "courseId", 1);
    ReflectionTestUtils.setField(sourceCourse, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(sourceCourse, "visibility", CourseVisibility.PRIVATE);

    when(courses.findByIdWithPlaces(1)).thenReturn(Optional.of(sourceCourse));
    when(courses.save(any(Course.class)))
        .thenAnswer(
            invocation -> {
              Course saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "courseId", 3);
              return saved;
            });

    // when
    Course result = service.copyCourse("member-1", 1);

    // then
    assertThat(result.getCourseId()).isEqualTo(3);
    verify(courses).save(any(Course.class));
  }

  @Test
  @DisplayName("타인의 비공개 코스는 복사 시도 시 404 예외를 던진다")
  void rejectsCopyingAnotherUsersPrivateCourse() {
    // given
    Course sourceCourse =
        Course.createDraft(
            "member-1",
            "타인 비공개 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(sourceCourse, "courseId", 1);
    ReflectionTestUtils.setField(sourceCourse, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(sourceCourse, "visibility", CourseVisibility.PRIVATE);

    when(courses.findByIdWithPlaces(1)).thenReturn(Optional.of(sourceCourse));

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.copyCourse("member-2", 1));
    verify(courses, never()).save(any());
  }

  @Test
  @DisplayName("DRAFT 상태의 코스는 복사 시도 시 CANNOT_COPY_DRAFT 예외를 던진다")
  void rejectsCopyingDraftCourse() {
    // given
    Course sourceCourse =
        Course.createDraft(
            "member-1",
            "임시 코스",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(sourceCourse, "courseId", 1);
    ReflectionTestUtils.setField(sourceCourse, "status", CourseStatus.DRAFT);
    ReflectionTestUtils.setField(sourceCourse, "visibility", CourseVisibility.PUBLIC);

    when(courses.findByIdWithPlaces(1)).thenReturn(Optional.of(sourceCourse));

    // when & then
    CourseException exception =
        assertThrows(CourseException.class, () -> service.copyCourse("member-1", 1));
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.CANNOT_COPY_DRAFT);
    verify(courses, never()).save(any());
  }

  @Test
  @DisplayName("존재하지 않거나 삭제된 코스를 복사 시도 시 404 예외를 던진다")
  void rejectsCopyingNonExistentOrDeletedCourse() {
    // given
    when(courses.findByIdWithPlaces(999)).thenReturn(Optional.empty());

    // when & then
    assertThrows(CourseNotFoundException.class, () -> service.copyCourse("member-1", 999));
    verify(courses, never()).save(any());
  }

  private CreateCourseCommand command() {
    LocalDateTime startAt = LocalDateTime.of(2026, 12, 1, 10, 0);
    return new CreateCourseCommand(
        "종로 데이트", List.of("감성적"), startAt, startAt.plusHours(4), CourseTransport.WALKING);
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

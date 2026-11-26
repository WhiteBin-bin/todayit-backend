package com.todayit.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayit.course.entity.Course;
import com.todayit.course.entity.CoursePlace;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.entity.Theme;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.exception.InvalidRegionPathException;
import com.todayit.course.repository.CourseMemberLikeRepository;
import com.todayit.course.repository.CourseRepository;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCourseConditionsCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceEntities;
import com.todayit.course.service.model.CourseLikeResult;
import com.todayit.course.service.model.CreateCourseResult;
import com.todayit.course.service.model.UpdateCourseTransportResult;
import com.todayit.course.service.model.UpdateCourseVisibilityResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseCommandService 단위 테스트")
class CourseCommandServiceTest {
  @Mock private CourseRepository courses;
  @Mock private CourseMemberLikeRepository courseMemberLikeRepository;
  @Mock private RegionQueryService regionQueryService;
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

    // when
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, null, 30);
    CourseException exception =
        assertThrows(CourseException.class, () -> service.addPlace("member-1", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.MAX_PLACE_DUPLICATION_EXCEEDED);
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("코스가 존재하지 않으면 장소 추가 시 404 예외를 던진다")
  void rejectsAddingPlaceWhenCourseNotFound() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(999, "member-1")).thenReturn(Optional.empty());

    // when
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(999, 10, null, 60);
    CourseException exception =
        assertThrows(CourseException.class, () -> service.addPlace("member-1", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("작성자가 아니면 장소 추가 시 404 예외를 던진다")
  void rejectsAddingPlaceWhenNotWriter() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(1, "member-2")).thenReturn(Optional.empty());

    // when
    AddCoursePlaceCommand command = new AddCoursePlaceCommand(1, 10, null, 60);
    CourseException exception =
        assertThrows(CourseException.class, () -> service.addPlace("member-2", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
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
    UpdateCoursePlaceOrdersCommand command =
        new UpdateCoursePlaceOrdersCommand(1, List.of(101, 999));

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.updatePlaceOrders("member-1", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_ORDER);
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

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.removePlace("member-1", 999, 101));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
    verify(courses, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("장소 삭제 시 작성자가 아니면 404 예외를 던진다")
  void rejectsRemovePlaceWhenNotWriter() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(1, "member-2")).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.removePlace("member-2", 1, 101));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
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

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.removePlace("member-1", 1, 999));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.COURSE_PLACE_NOT_FOUND);
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

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.deleteCourse("member-1", 999));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
    verify(courses, never()).delete(any());
  }

  @Test
  @DisplayName("코스 삭제 시 작성자가 아니면 404 예외를 던진다")
  void rejectsDeleteCourseWhenNotWriter() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(1, "member-2")).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.deleteCourse("member-2", 1));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
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

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.deleteCourse("member-1", 1));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
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

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.copyCourse("member-2", 1));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
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

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.copyCourse("member-1", 1));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.CANNOT_COPY_DRAFT);
    verify(courses, never()).save(any());
  }

  @Test
  @DisplayName("존재하지 않거나 삭제된 코스를 복사 시도 시 404 예외를 던진다")
  void rejectsCopyingNonExistentOrDeletedCourse() {
    // given
    when(courses.findByIdWithPlaces(999)).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.copyCourse("member-1", 999));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
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

  @Test
  @DisplayName("공개 확정 코스에 좋아요를 정상적으로 등록하고 최신 좋아요 수를 반환한다")
  void likesCourseSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "공개 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findById(1)).thenReturn(Optional.of(course));
    when(courseMemberLikeRepository.insertIfAbsent("liker-member", 1)).thenReturn(1);
    when(courseMemberLikeRepository.countByCourseId(1)).thenReturn(5L);

    // when
    CourseLikeResult result = service.likeCourse(1, "liker-member");

    // then
    assertThat(result.courseId()).isEqualTo(1);
    assertThat(result.liked()).isTrue();
    assertThat(result.likeCount()).isEqualTo(5L);
    verify(courseMemberLikeRepository).insertIfAbsent("liker-member", 1);
  }

  @Test
  @DisplayName("이미 좋아요를 누른 코스에 다시 좋아요를 요청하면 409 예외를 던진다")
  void rejectsDuplicateLike() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "공개 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findById(1)).thenReturn(Optional.of(course));
    when(courseMemberLikeRepository.insertIfAbsent("liker-member", 1)).thenReturn(0);

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.likeCourse(1, "liker-member"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.ALREADY_LIKED);
  }

  @Test
  @DisplayName("존재하지 않는 코스에 좋아요를 요청하면 404 예외를 던진다")
  void rejectsLikingNonExistentCourse() {
    // given
    when(courses.findById(999)).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.likeCourse(999, "liker-member"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("삭제된 코스에 좋아요를 요청하면 404 예외를 던진다")
  void rejectsLikingDeletedCourse() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "삭제된 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.DELETED);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findById(1)).thenReturn(Optional.of(course));

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.likeCourse(1, "liker-member"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("DRAFT 상태의 미확정 코스에 좋아요를 요청하면 404 예외를 던진다")
  void rejectsLikingDraftCourse() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시저장 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.DRAFT);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findById(1)).thenReturn(Optional.of(course));

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.likeCourse(1, "liker-member"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("타인의 비공개 코스에 좋아요를 요청하면 존재 여부를 숨기기 위해 404 예외를 던진다")
  void rejectsLikingOthersPrivateCourse() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "비공개 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PRIVATE);

    when(courses.findById(1)).thenReturn(Optional.of(course));

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.likeCourse(1, "other-member"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("본인의 비공개 활성 코스에는 좋아요를 등록할 수 있다")
  void likesOwnPrivateActiveCourseSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "내 비공개 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PRIVATE);

    when(courses.findById(1)).thenReturn(Optional.of(course));
    when(courseMemberLikeRepository.insertIfAbsent("owner-member", 1)).thenReturn(1);
    when(courseMemberLikeRepository.countByCourseId(1)).thenReturn(1L);

    // when
    CourseLikeResult result = service.likeCourse(1, "owner-member");

    // then
    assertThat(result.courseId()).isEqualTo(1);
    assertThat(result.liked()).isTrue();
    assertThat(result.likeCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("좋아요 등록 시 코스가 동시 삭제되어 외래키 위반이 발생하면 404 예외를 던진다")
  void throwsCourseNotFoundWhenCourseDeletedConcurrentlyDuringLike() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "내 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            LocalDateTime.of(2026, 12, 1, 14, 0),
            List.of(),
            LocalDateTime.of(2026, 11, 24, 9, 0));
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findById(1)).thenReturn(Optional.of(course));
    when(courseMemberLikeRepository.insertIfAbsent("liker-member", 1))
        .thenThrow(
            new DataIntegrityViolationException(
                "violates foreign key constraint fk_course_member_like_course"));

    // when
    CourseException exception =
        assertThrows(CourseException.class, () -> service.likeCourse(1, "liker-member"));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @ParameterizedTest
  @ValueSource(strings = {"some other data integrity violation", "fk_course_member_like_member"})
  @DisplayName("좋아요 등록 시 코스 외래키 외의 다른 무결성 위반은 예외를 그대로 전파한다")
  void propagatesOtherDataIntegrityViolationsDuringLike(String causeMessage) {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "내 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            LocalDateTime.of(2026, 12, 1, 14, 0),
            List.of(),
            LocalDateTime.of(2026, 11, 24, 9, 0));
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findById(1)).thenReturn(Optional.of(course));
    DataIntegrityViolationException failure =
        new DataIntegrityViolationException(
            "could not execute statement [insert into course_member_like (member_id, course_id)"
                + " values (?, ?)]",
            new RuntimeException(causeMessage));
    when(courseMemberLikeRepository.insertIfAbsent("liker-member", 1)).thenThrow(failure);

    // when
    DataIntegrityViolationException exception =
        assertThrows(
            DataIntegrityViolationException.class, () -> service.likeCourse(1, "liker-member"));

    // then
    assertThat(exception).isSameAs(failure);
  }

  @Test
  @DisplayName("확정된 코스의 공개 범위를 성공적으로 변경한다")
  void updatesCourseVisibilitySuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "내 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));

    // when
    UpdateCourseVisibilityResult result =
        service.updateCourseVisibility("owner-member", 1, CourseVisibility.PRIVATE);

    // then
    assertThat(result.courseId()).isEqualTo(1);
    assertThat(result.visibility()).isEqualTo(CourseVisibility.PRIVATE);
    assertThat(result.updatedAt()).isNotNull();
    verify(courses).updateVisibility(eq(1), eq(CourseVisibility.PRIVATE), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("코스가 존재하지 않거나 타인의 코스 또는 삭제된 코스이면 404 예외를 던진다")
  void rejectsUpdatingVisibilityWhenCourseNotFoundOrDeleted() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(999, "owner-member")).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(
            CourseException.class,
            () -> service.updateCourseVisibility("owner-member", 999, CourseVisibility.PUBLIC));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("임시저장(DRAFT) 상태의 코스는 공개 범위를 변경할 수 없다")
  void rejectsUpdatingVisibilityForDraftCourse() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시저장 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.DRAFT);
    ReflectionTestUtils.setField(course, "visibility", CourseVisibility.PUBLIC);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));

    // when
    CourseException exception =
        assertThrows(
            CourseException.class,
            () -> service.updateCourseVisibility("owner-member", 1, CourseVisibility.PRIVATE));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.CANNOT_CHANGE_DRAFT_VISIBILITY);
  }

  @Test
  @DisplayName("코스가 존재하지 않거나 타인의 코스 또는 삭제된 코스이면 조건 수정 시 404 예외를 던진다")
  void rejectsUpdatingConditionsWhenCourseNotFoundOrDeleted() {
    // given
    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(999, null, null, null, null, null);
    when(courses.findByIdAndMemberIdForUpdate(999, "owner-member")).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(
            CourseException.class, () -> service.updateCourseConditions("owner-member", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
  }

  @Test
  @DisplayName("확정(ACTIVE) 상태의 코스는 생성 조건을 수정할 수 없다")
  void rejectsUpdatingConditionsForActiveCourse() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "확정 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, null, null, null, null, CourseTransport.WALKING);

    // when
    CourseException exception =
        assertThrows(
            CourseException.class, () -> service.updateCourseConditions("owner-member", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.CANNOT_EDIT_ACTIVE_COURSE);
  }

  @Test
  @DisplayName("유효하지 않은 지역 코드이거나 존재하지 않는 지역인 경우 예외를 던진다")
  void rejectsUpdatingConditionsWhenInvalidRegionCode() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));
    when(regionQueryService.findRegionIds("99", null)).thenThrow(new InvalidRegionPathException());

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, "99", null, null, null, null);

    // when
    CourseException exception =
        assertThrows(
            CourseException.class, () -> service.updateCourseConditions("owner-member", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_REGION);
  }

  @Test
  @DisplayName("durationMinutes가 제공되고 endAt이 없는 경우 endAt이 자동 계산되어 리포지토리를 통해 갱신된다")
  void updatesConditionsSuccessfullyWithDurationMinutes() {
    // given
    LocalDateTime initialStart = LocalDateTime.of(2026, 12, 1, 10, 0);
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            initialStart,
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);

    Course updatedCourse =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.WALKING,
            initialStart,
            initialStart.plusMinutes(120),
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(updatedCourse, "courseId", 1);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member"))
        .thenReturn(Optional.of(course), Optional.of(updatedCourse));

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, null, null, null, 120, CourseTransport.WALKING);

    // when
    Course updated = service.updateCourseConditions("owner-member", command);

    // then
    assertThat(updated.getStartAt()).isEqualTo(initialStart);
    assertThat(updated.getEndAt()).isEqualTo(initialStart.plusMinutes(120));
    assertThat(updated.getTransport()).isEqualTo(CourseTransport.WALKING);
    verify(courses)
        .updateConditions(
            eq(1),
            eq(initialStart),
            eq(initialStart.plusMinutes(120)),
            eq(CourseTransport.WALKING),
            any(LocalDateTime.class));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("시작 시각 없이 소요 시간을 요청하면 지역과 일정을 저장하기 전에 거부한다")
  void rejectsDurationWithoutStartAtBeforeSaving(boolean providesRegion) {
    // given
    LocalDateTime now = LocalDateTime.of(2026, 12, 1, 9, 0);
    Course source =
        Course.createDraft(
            "owner-member", "원본 코스", CourseTransport.CAR, now, now.plusHours(4), List.of(), now);
    Course course = Course.createCopyDraft("owner-member", source, now);
    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));
    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, providesRegion ? "11" : null, null, null, 120, null);

    // when
    CourseException exception =
        assertThrows(
            CourseException.class, () -> service.updateCourseConditions("owner-member", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.START_AT_REQUIRED);
    verifyNoInteractions(regionQueryService);
    verify(courses, never()).saveAndFlush(any());
    verify(courses, never()).updateConditions(eq(1), any(), any(), any(), any());
    assertThat(course.getStartAt()).isNull();
    assertThat(course.getEndAt()).isNull();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("종료 시각과 소요 시간을 함께 보내면 유효한 시작 시각에 소요 시간을 더해 저장한다")
  void prioritizesDurationOverExplicitEndAt(boolean providesStartAt) {
    // given
    LocalDateTime initialStart = LocalDateTime.of(2026, 12, 1, 9, 0);
    LocalDateTime requestedStart = LocalDateTime.of(2026, 12, 1, 10, 0);
    LocalDateTime explicitEnd = LocalDateTime.of(2026, 12, 1, 14, 0);
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            initialStart,
            explicitEnd,
            List.of(),
            initialStart);
    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));
    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(
            1, null, providesStartAt ? requestedStart : null, explicitEnd, 60, null);

    // when
    service.updateCourseConditions("owner-member", command);

    // then
    LocalDateTime effectiveStart = providesStartAt ? requestedStart : initialStart;
    verify(courses)
        .updateConditions(
            eq(1),
            eq(effectiveStart),
            eq(effectiveStart.plusMinutes(60)),
            eq(CourseTransport.CAR),
            any(LocalDateTime.class));
  }

  @Test
  @DisplayName("지역 코드(시도)와 일시, 이동수단을 지정하여 정상적으로 조건을 수정한다")
  void updatesConditionsSuccessfullyWithExplicitEndAtAndRegion() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);

    LocalDateTime newStartAt = LocalDateTime.of(2026, 12, 2, 9, 0);
    LocalDateTime newEndAt = LocalDateTime.of(2026, 12, 2, 18, 0);

    Course updatedCourse =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.PUBLIC_TRANSPORT,
            newStartAt,
            newEndAt,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(updatedCourse, "courseId", 1);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member"))
        .thenReturn(Optional.of(course), Optional.of(updatedCourse));
    when(regionQueryService.findRegionIds("11", null)).thenReturn(List.of(10, 11));

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(
            1, "11", newStartAt, newEndAt, null, CourseTransport.PUBLIC_TRANSPORT);

    // when
    Course updated = service.updateCourseConditions("owner-member", command);

    // then
    assertThat(updated.getStartAt()).isEqualTo(newStartAt);
    assertThat(updated.getEndAt()).isEqualTo(newEndAt);
    assertThat(updated.getTransport()).isEqualTo(CourseTransport.PUBLIC_TRANSPORT);
    verify(courses)
        .updateConditions(
            eq(1),
            eq(newStartAt),
            eq(newEndAt),
            eq(CourseTransport.PUBLIC_TRANSPORT),
            any(LocalDateTime.class));
  }

  @Test
  @DisplayName("임시저장(DRAFT) 코스의 이동수단을 정상 변경한다")
  void updatesTransportSuccessfully() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));

    // when
    UpdateCourseTransportResult result =
        service.updateCourseTransport("owner-member", 1, CourseTransport.WALKING);

    // then
    assertThat(result.courseId()).isEqualTo(1);
    assertThat(result.transport()).isEqualTo(CourseTransport.WALKING);
    verify(courses).updateTransport(eq(1), eq(CourseTransport.WALKING), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("확정(ACTIVE) 상태의 코스는 이동수단을 변경할 수 없다")
  void rejectsUpdatingTransportForActiveCourse() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "확정 코스",
            CourseTransport.CAR,
            LocalDateTime.now(),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);
    ReflectionTestUtils.setField(course, "status", CourseStatus.ACTIVE);
    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));

    // when
    CourseException exception =
        assertThrows(
            CourseException.class,
            () -> service.updateCourseTransport("owner-member", 1, CourseTransport.WALKING));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.CANNOT_EDIT_ACTIVE_COURSE);
    verify(courses, never())
        .updateTransport(any(Integer.class), any(CourseTransport.class), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("코스가 존재하지 않거나 타인의 코스 또는 삭제된 코스이면 이동수단 변경 시 404 예외를 던진다")
  void rejectsUpdatingTransportWhenCourseNotFoundOrDeleted() {
    // given
    when(courses.findByIdAndMemberIdForUpdate(999, "owner-member")).thenReturn(Optional.empty());

    // when
    CourseException exception =
        assertThrows(
            CourseException.class,
            () -> service.updateCourseTransport("owner-member", 999, CourseTransport.WALKING));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.NOT_FOUND);
    verify(courses, never())
        .updateTransport(any(Integer.class), any(CourseTransport.class), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("3자리 시군구 코드가 둘 이상의 시·도에 걸쳐 중복되면 유일하지 않으므로 예외를 던진다")
  void throwsInvalidRegionWhenSigunguCodeSpansMultipleSidos() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member")).thenReturn(Optional.of(course));
    when(regionQueryService.findRegionIds(null, "110")).thenReturn(List.of(1, 2));
    when(regionQueryService.hasMultipleSidos(List.of(1, 2))).thenReturn(true);

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, "110", null, null, null, null);

    // when
    CourseException exception =
        assertThrows(
            CourseException.class, () -> service.updateCourseConditions("owner-member", command));

    // then
    assertThat(exception.getErrorCode()).isEqualTo(CourseErrorCode.INVALID_REGION);
  }

  @Test
  @DisplayName("5자리 시도+시군구 코드로 명확하게 단일 시군구를 지정하여 조건을 수정한다")
  void updatesConditionsSuccessfullyWithFiveDigitRegionCode() {
    // given
    Course course =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(course, "courseId", 1);

    Course updatedCourse =
        Course.createDraft(
            "owner-member",
            "임시 코스",
            CourseTransport.CAR,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            List.of(),
            LocalDateTime.now());
    ReflectionTestUtils.setField(updatedCourse, "courseId", 1);

    when(courses.findByIdAndMemberIdForUpdate(1, "owner-member"))
        .thenReturn(Optional.of(course), Optional.of(updatedCourse));
    when(regionQueryService.findRegionIds("11", "110")).thenReturn(List.of(1));

    UpdateCourseConditionsCommand command =
        new UpdateCourseConditionsCommand(1, "11110", null, null, null, null);

    // when
    Course updated = service.updateCourseConditions("owner-member", command);

    // then
    assertThat(updated).isNotNull();
    verify(regionQueryService).findRegionIds("11", "110");
  }
}

package com.todayit.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayit.common.exception.GlobalExceptionHandler;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.exception.CourseNotFoundException;
import com.todayit.course.exception.CoursePlaceNotFoundException;
import com.todayit.course.exception.InvalidCoursePlaceOrderException;
import com.todayit.course.exception.MaxPlaceDuplicationExceededException;
import com.todayit.course.exception.PlaceNotFoundException;
import com.todayit.course.service.CourseFacade;
import com.todayit.course.service.command.AddCoursePlaceCommand;
import com.todayit.course.service.command.CreateCourseCommand;
import com.todayit.course.service.command.UpdateCoursePlaceOrdersCommand;
import com.todayit.course.service.model.AddCoursePlaceResult;
import com.todayit.course.service.model.CopyCourseResult;
import com.todayit.course.service.model.CourseDetailResult;
import com.todayit.course.service.model.CoursePlaceDetailResult;
import com.todayit.course.service.model.CreateCourseResult;
import com.todayit.course.service.model.RemoveCoursePlaceResult;
import com.todayit.course.service.model.UpdateCoursePlaceOrdersResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("코스 생성 HTTP 계약")
class CourseControllerTest {
  @Mock private CourseFacade courseFacade;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(new CourseController(courseFacade))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("인증된 회원으로 코스를 생성하고 DRAFT의 courseId를 반환한다")
  void createsDraftUsingAuthenticatedMember() throws Exception {
    // given
    LocalDateTime createdAt = LocalDateTime.of(2026, 11, 17, 12, 0);
    when(courseFacade.createCourse(eq("member-1"), any())).thenReturn(new CreateCourseResult(12));
    String request =
        """
        {
          "title": "  종로 데이트  ",
          "themes": [
            "감성적",
            "여유로움"
          ],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.courseId").value(12));

    CreateCourseCommand expected =
        new CreateCourseCommand(
            "종로 데이트",
            List.of("감성적", "여유로움"),
            LocalDateTime.of(2026, 12, 1, 10, 0),
            LocalDateTime.of(2026, 12, 1, 18, 0),
            CourseTransport.WALKING);
    verify(courseFacade).createCourse("member-1", expected);
  }

  @Test
  @DisplayName("코스명이 비어 있으면 400과 INVALID_COURSE_TITLE을 반환한다")
  void rejectsEmptyTitle() throws Exception {
    // given
    String request =
        """
        {
          "title": "   ",
          "themes": ["감성적"],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_TITLE"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("코스명이 100자를 초과하면 400과 INVALID_COURSE_TITLE을 반환한다")
  void rejectsTooLongTitle() throws Exception {
    // given
    String request =
        """
        {
          "title": "%s",
          "themes": ["감성적"],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """
            .formatted("a".repeat(101));

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_TITLE"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("콘셉트가 비어 있으면 400과 INVALID_COURSE_THEMES를 반환한다")
  void rejectsEmptyThemes() throws Exception {
    // given
    String request =
        """
        {
          "title": "코스명",
          "themes": [],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_THEMES"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("콘셉트가 2개를 초과하면 400과 INVALID_COURSE_THEMES를 반환한다")
  void rejectsTooManyThemes() throws Exception {
    // given
    String request =
        """
        {
          "title": "코스명",
          "themes": ["감성적", "여유로움", "자연친화적"],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_THEMES"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("콘셉트에 중복된 값이 있으면 400과 INVALID_COURSE_THEMES를 반환한다")
  void rejectsDuplicateThemes() throws Exception {
    // given
    String request =
        """
        {
          "title": "코스명",
          "themes": ["감성적", "감성적"],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_THEMES"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("시작 시각이 누락되면 400과 COURSE_START_AT_REQUIRED를 반환한다")
  void rejectsMissingStartAt() throws Exception {
    // given
    String request =
        """
        {
          "title": "코스명",
          "themes": ["감성적"],
          "endAt": "2026-12-01T18:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_START_AT_REQUIRED"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("종료 시각이 시작 시각 이전이거나 같으면 400과 INVALID_COURSE_END_AT을 반환한다")
  void rejectsEndAtNotAfterStartAt() throws Exception {
    // given
    String request =
        """
        {
          "title": "코스명",
          "themes": ["감성적"],
          "startAt": "2026-12-01T10:00:00",
          "endAt": "2026-12-01T10:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_END_AT"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("종료 시각은 null이어도 허용된다")
  void allowsNullEndAt() throws Exception {
    // given
    when(courseFacade.createCourse(eq("member-1"), any())).thenReturn(new CreateCourseResult(1));
    String request =
        """
        {
          "title": "코스명",
          "themes": ["감성적"],
          "startAt": "2026-12-01T10:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result.andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("이동수단이 누락되면 400과 COURSE_TRANSPORT_REQUIRED를 반환한다")
  void rejectsMissingTransport() throws Exception {
    // given
    String request =
        """
        {
          "title": "코스명",
          "themes": ["감성적"],
          "startAt": "2026-12-01T10:00:00"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_TRANSPORT_REQUIRED"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("지원하지 않는 콘셉트는 400과 UNSUPPORTED_COURSE_THEME을 반환한다")
  void returnsBadRequestWhenUnsupportedTheme() throws Exception {
    // given
    when(courseFacade.createCourse(eq("member-1"), any()))
        .thenThrow(new CourseException(CourseErrorCode.UNSUPPORTED_THEME));
    String request =
        """
        {
          "title": "코스명",
          "themes": ["우주여행"],
          "startAt": "2026-12-01T10:00:00",
          "transport": "WALKING"
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("UNSUPPORTED_COURSE_THEME"));
  }

  @Test
  @DisplayName("코스 상세 조회 시 작성자 본인이면 isWriter가 true이고 전체 정보가 반환된다")
  void getsCourseDetailAsWriter() throws Exception {
    // given
    LocalDateTime now = LocalDateTime.of(2026, 11, 17, 12, 0);
    CoursePlaceDetailResult place =
        new CoursePlaceDetailResult(
            1,
            10,
            "블루보틀 삼청",
            "CAFE_DESSERT",
            "서울특별시 종로구 북촌로 5길",
            new BigDecimal("37.58123400"),
            new BigDecimal("126.98123400"),
            "https://example.com/place.jpg",
            1,
            60);
    CourseDetailResult detail =
        new CourseDetailResult(
            1,
            "종로 데이트 코스",
            "종로 감성 카페 투어",
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            LocalDateTime.of(2026, 12, 1, 18, 0),
            CourseStatus.DRAFT,
            CourseVisibility.PRIVATE,
            0,
            "11",
            "서울특별시",
            "110",
            "종로구",
            List.of("감성적", "여유로움"),
            true,
            List.of(place),
            now,
            now,
            false,
            false,
            List.of("웨이팅이 있을 수 있습니다."));
    when(courseFacade.getCourseDetail(1, "member-1")).thenReturn(detail);

    // when
    ResultActions result =
        mvc.perform(
            get("/api/v1/courses/1")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.courseId").value(1))
        .andExpect(jsonPath("$.data.title").value("종로 데이트 코스"))
        .andExpect(jsonPath("$.data.courseSummary").value("종로 감성 카페 투어"))
        .andExpect(jsonPath("$.data.transport").value("WALKING"))
        .andExpect(jsonPath("$.data.status").value("DRAFT"))
        .andExpect(jsonPath("$.data.visibility").value("PRIVATE"))
        .andExpect(jsonPath("$.data.isWriter").value(true))
        .andExpect(jsonPath("$.data.liked").value(false))
        .andExpect(jsonPath("$.data.scrapped").value(false))
        .andExpect(jsonPath("$.data.beforeVisit[0]").value("웨이팅이 있을 수 있습니다."))
        .andExpect(jsonPath("$.data.sidoCode").value("11"))
        .andExpect(jsonPath("$.data.sidoName").value("서울특별시"))
        .andExpect(jsonPath("$.data.sigunguCode").value("110"))
        .andExpect(jsonPath("$.data.sigunguName").value("종로구"))
        .andExpect(jsonPath("$.data.themes[0]").value("감성적"))
        .andExpect(jsonPath("$.data.themes[1]").value("여유로움"))
        .andExpect(jsonPath("$.data.places[0].coursePlaceId").value(1))
        .andExpect(jsonPath("$.data.places[0].placeId").value(10))
        .andExpect(jsonPath("$.data.places[0].name").value("블루보틀 삼청"))
        .andExpect(jsonPath("$.data.places[0].category").value("CAFE_DESSERT"))
        .andExpect(jsonPath("$.data.places[0].order").value(1))
        .andExpect(jsonPath("$.data.places[0].stayMinute").value(60));
  }

  @Test
  @DisplayName("비회원 코스 상세 조회 시 authentication이 null이어도 정상 조회된다")
  void getsCourseDetailAsAnonymous() throws Exception {
    // given
    LocalDateTime now = LocalDateTime.of(2026, 11, 17, 12, 0);
    CourseDetailResult detail =
        new CourseDetailResult(
            1,
            "종로 공개 코스",
            null,
            CourseTransport.WALKING,
            LocalDateTime.of(2026, 12, 1, 10, 0),
            null,
            CourseStatus.ACTIVE,
            CourseVisibility.PUBLIC,
            5,
            null,
            null,
            null,
            null,
            List.of(),
            false,
            List.of(),
            now,
            now,
            false,
            false,
            List.of());
    when(courseFacade.getCourseDetail(1, null)).thenReturn(detail);

    // when
    ResultActions result = mvc.perform(get("/api/v1/courses/1"));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.courseId").value(1))
        .andExpect(jsonPath("$.data.isWriter").value(false))
        .andExpect(jsonPath("$.data.liked").value(false))
        .andExpect(jsonPath("$.data.scrapped").value(false))
        .andExpect(jsonPath("$.data.beforeVisit").isEmpty())
        .andExpect(jsonPath("$.data.places").isEmpty());
  }

  @Test
  @DisplayName("존재하지 않거나 접근 권한이 없는 코스는 404와 COURSE_NOT_FOUND를 반환한다")
  void returnsNotFoundWhenCourseCannotBeAccessed() throws Exception {
    // given
    when(courseFacade.getCourseDetail(99, "member-1")).thenThrow(new CourseNotFoundException());

    // when
    ResultActions result =
        mvc.perform(
            get("/api/v1/courses/99")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("코스를 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("코스에 장소를 정상 추가하고 201과 코스 장소 정보를 반환한다")
  void addsPlaceToCourseSuccessfully() throws Exception {
    // given
    CoursePlaceDetailResult placeDetail =
        new CoursePlaceDetailResult(
            1,
            10,
            "블루보틀 삼청",
            "CAFE_DESSERT",
            "서울특별시 종로구 북촌로 5길",
            new BigDecimal("37.58123400"),
            new BigDecimal("126.98123400"),
            "https://example.com/place.jpg",
            1,
            60);
    when(courseFacade.addPlaceToCourse(eq("member-1"), any(AddCoursePlaceCommand.class)))
        .thenReturn(new AddCoursePlaceResult(placeDetail, List.of(placeDetail)));
    String request =
        """
        {
          "placeId": 10,
          "order": 1,
          "stayMinute": 60
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.addedPlace.coursePlaceId").value(1))
        .andExpect(jsonPath("$.data.addedPlace.placeId").value(10))
        .andExpect(jsonPath("$.data.addedPlace.order").value(1))
        .andExpect(jsonPath("$.data.addedPlace.stayMinute").value(60))
        .andExpect(jsonPath("$.data.places").isArray());
    verify(courseFacade).addPlaceToCourse("member-1", new AddCoursePlaceCommand(1, 10, 1, 60));
  }

  @Test
  @DisplayName("장소 식별자가 없으면 400과 INVALID_PLACE_ID를 반환한다")
  void rejectsMissingPlaceId() throws Exception {
    // given
    String request =
        """
        {
          "placeId": null,
          "order": 1,
          "stayMinute": 60
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_PLACE_ID"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("방문 순서가 1 미만이면 400과 INVALID_COURSE_PLACE_ORDER를 반환한다")
  void rejectsInvalidOrder() throws Exception {
    // given
    String request =
        """
        {
          "placeId": 10,
          "order": 0,
          "stayMinute": 60
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_PLACE_ORDER"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("체류 시간이 0 미만이면 400과 INVALID_STAY_MINUTE을 반환한다")
  void rejectsNegativeStayMinute() throws Exception {
    // given
    String request =
        """
        {
          "placeId": 10,
          "order": 1,
          "stayMinute": -10
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_STAY_MINUTE"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("체류 시간이 1440분을 초과하면 400과 INVALID_STAY_MINUTE을 반환한다")
  void rejectsExcessiveStayMinute() throws Exception {
    // given
    String request =
        """
        {
          "placeId": 10,
          "order": 1,
          "stayMinute": 1441
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_STAY_MINUTE"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("방문 순서와 체류 시간을 생략하면 기본값으로 추가된다")
  void allowsOmittingOrderAndStayMinute() throws Exception {
    // given
    CoursePlaceDetailResult placeDetail =
        new CoursePlaceDetailResult(
            1,
            10,
            "블루보틀 삼청",
            "CAFE_DESSERT",
            "서울특별시 종로구 북촌로 5길",
            new BigDecimal("37.58123400"),
            new BigDecimal("126.98123400"),
            "https://example.com/place.jpg",
            1,
            0);
    when(courseFacade.addPlaceToCourse(eq("member-1"), any(AddCoursePlaceCommand.class)))
        .thenReturn(new AddCoursePlaceResult(placeDetail, List.of(placeDetail)));
    String request =
        """
        {
          "placeId": 10
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.addedPlace.order").value(1))
        .andExpect(jsonPath("$.data.addedPlace.stayMinute").value(0))
        .andExpect(jsonPath("$.data.places").isArray());
    verify(courseFacade).addPlaceToCourse("member-1", new AddCoursePlaceCommand(1, 10, null, 0));
  }

  @Test
  @DisplayName("코스를 찾을 수 없으면 404와 COURSE_NOT_FOUND를 반환한다")
  void returnsNotFoundWhenCourseNotFoundOnAddPlace() throws Exception {
    // given
    doThrow(new CourseNotFoundException())
        .when(courseFacade)
        .addPlaceToCourse(eq("member-1"), any(AddCoursePlaceCommand.class));
    String request =
        """
        {
          "placeId": 10
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/999/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
  }

  @Test
  @DisplayName("장소를 찾을 수 없거나 삭제된 장소이면 404와 PLACE_NOT_FOUND를 반환한다")
  void returnsNotFoundWhenPlaceNotFoundOnAddPlace() throws Exception {
    // given
    doThrow(new PlaceNotFoundException())
        .when(courseFacade)
        .addPlaceToCourse(eq("member-1"), any(AddCoursePlaceCommand.class));
    String request =
        """
        {
          "placeId": 999
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("PLACE_NOT_FOUND"));
  }

  @Test
  @DisplayName("동일 장소 중복 추가 횟수(2회)를 초과하면 400과 MAX_PLACE_DUPLICATION_EXCEEDED를 반환한다")
  void returnsBadRequestWhenMaxDuplicationExceeded() throws Exception {
    // given
    doThrow(new MaxPlaceDuplicationExceededException())
        .when(courseFacade)
        .addPlaceToCourse(eq("member-1"), any(AddCoursePlaceCommand.class));
    String request =
        """
        {
          "placeId": 10
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/places")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("MAX_PLACE_DUPLICATION_EXCEEDED"));
  }

  @Test
  @DisplayName("코스 장소의 방문 순서를 일괄 변경하고 200과 성공 결과를 반환한다")
  void updatesCoursePlaceOrdersSuccessfully() throws Exception {
    // given
    when(courseFacade.updateCoursePlaceOrders(
            eq("member-1"), any(UpdateCoursePlaceOrdersCommand.class)))
        .thenReturn(new UpdateCoursePlaceOrdersResult(List.of()));
    String request =
        """
        {
          "coursePlaceIds": [3, 1, 2]
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/places/order")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.places").isArray());
    verify(courseFacade)
        .updateCoursePlaceOrders(
            "member-1", new UpdateCoursePlaceOrdersCommand(1, List.of(3, 1, 2)));
  }

  @Test
  @DisplayName("순서 변경 요청 시 식별자 목록이 비어 있으면 400을 반환한다")
  void rejectsEmptyCoursePlaceIdsOnReorder() throws Exception {
    // given
    String request =
        """
        {
          "coursePlaceIds": []
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/places/order")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_PLACE_ORDER"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("순서 변경 요청 시 중복된 식별자가 포함되면 400을 반환한다")
  void rejectsDuplicateCoursePlaceIdsOnReorder() throws Exception {
    // given
    String request =
        """
        {
          "coursePlaceIds": [1, 1, 2]
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/places/order")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_PLACE_ORDER"));
    verifyNoInteractions(courseFacade);
  }

  @Test
  @DisplayName("순서 변경 시 코스를 찾을 수 없거나 작성자가 아니면 404를 반환한다")
  void returnsNotFoundWhenCourseNotFoundOnReorder() throws Exception {
    // given
    doThrow(new CourseNotFoundException())
        .when(courseFacade)
        .updateCoursePlaceOrders(eq("member-1"), any(UpdateCoursePlaceOrdersCommand.class));
    String request =
        """
        {
          "coursePlaceIds": [3, 1, 2]
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/999/places/order")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
  }

  @Test
  @DisplayName("순서 변경 시 전달된 장소 목록이 코스 장소와 일치하지 않으면 400을 반환한다")
  void returnsBadRequestWhenFacadeThrowsInvalidCoursePlaceOrderException() throws Exception {
    // given
    doThrow(new InvalidCoursePlaceOrderException())
        .when(courseFacade)
        .updateCoursePlaceOrders(eq("member-1"), any(UpdateCoursePlaceOrdersCommand.class));
    String request =
        """
        {
          "coursePlaceIds": [3, 1, 2]
        }
        """;

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/places/order")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_COURSE_PLACE_ORDER"));
  }

  @Test
  @DisplayName("코스 장소를 정상적으로 삭제하고 200과 삭제 결과를 반환한다")
  void deletesCoursePlaceSuccessfully() throws Exception {
    // given
    when(courseFacade.removePlaceFromCourse("member-1", 1, 10))
        .thenReturn(new RemoveCoursePlaceResult(10, List.of()));

    // when
    ResultActions result =
        mvc.perform(
            delete("/api/v1/courses/1/places/10")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.deletedCoursePlaceId").value(10))
        .andExpect(jsonPath("$.data.places").isArray());
    verify(courseFacade).removePlaceFromCourse("member-1", 1, 10);
  }

  @Test
  @DisplayName("장소 삭제 시 코스를 찾을 수 없거나 작성자가 아니면 404를 반환한다")
  void returnsNotFoundWhenCourseNotFoundOnDeletePlace() throws Exception {
    // given
    doThrow(new CourseNotFoundException())
        .when(courseFacade)
        .removePlaceFromCourse("member-1", 999, 10);

    // when
    ResultActions result =
        mvc.perform(
            delete("/api/v1/courses/999/places/10")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
  }

  @Test
  @DisplayName("장소 삭제 시 코스 내 장소를 찾을 수 없으면 404를 반환한다")
  void returnsNotFoundWhenCoursePlaceNotFoundOnDeletePlace() throws Exception {
    // given
    doThrow(new CoursePlaceNotFoundException())
        .when(courseFacade)
        .removePlaceFromCourse("member-1", 1, 999);

    // when
    ResultActions result =
        mvc.perform(
            delete("/api/v1/courses/1/places/999")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_PLACE_NOT_FOUND"));
  }

  @Test
  @DisplayName("코스를 정상적으로 영구 삭제하고 204를 반환한다")
  void deletesCourseSuccessfully() throws Exception {
    // when
    ResultActions result =
        mvc.perform(
            delete("/api/v1/courses/1")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result.andExpect(status().isNoContent());
    verify(courseFacade).deleteCourse("member-1", 1);
  }

  @Test
  @DisplayName("코스 삭제 시 코스를 찾을 수 없거나 작성자가 아니면 404를 반환한다")
  void returnsNotFoundWhenCourseNotFoundOnDeleteCourse() throws Exception {
    // given
    doThrow(new CourseNotFoundException()).when(courseFacade).deleteCourse("member-1", 999);

    // when
    ResultActions result =
        mvc.perform(
            delete("/api/v1/courses/999")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
  }

  @Test
  @DisplayName("확정된 코스를 정상적으로 복사하고 201과 복사된 코스 정보를 반환한다")
  void copiesCourseSuccessfully() throws Exception {
    // given
    LocalDateTime now = LocalDateTime.of(2026, 11, 19, 12, 0);
    when(courseFacade.copyCourse("member-1", 1))
        .thenReturn(new CopyCourseResult(2, 1, null, CourseStatus.DRAFT, List.of(), now));

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/copy")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.courseId").value(2))
        .andExpect(jsonPath("$.data.originalCourseId").value(1))
        .andExpect(jsonPath("$.data.title").doesNotExist())
        .andExpect(jsonPath("$.data.status").value("DRAFT"))
        .andExpect(jsonPath("$.data.places").isArray())
        .andExpect(jsonPath("$.data.createdAt").exists());
    verify(courseFacade).copyCourse("member-1", 1);
  }

  @Test
  @DisplayName("코스 복사 시 코스를 찾을 수 없거나 타인의 비공개 코스이면 404를 반환한다")
  void returnsNotFoundWhenCourseNotFoundOnCopyCourse() throws Exception {
    // given
    doThrow(new CourseNotFoundException()).when(courseFacade).copyCourse("member-1", 999);

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/999/copy")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("COURSE_NOT_FOUND"));
  }

  @Test
  @DisplayName("코스 복사 시 DRAFT 상태의 코스이면 400과 CANNOT_COPY_DRAFT를 반환한다")
  void returnsBadRequestWhenCopyCourseIsDraft() throws Exception {
    // given
    doThrow(new CourseException(CourseErrorCode.CANNOT_COPY_DRAFT))
        .when(courseFacade)
        .copyCourse("member-1", 1);

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses/1/copy")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null, List.of())));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("CANNOT_COPY_DRAFT"));
  }
}

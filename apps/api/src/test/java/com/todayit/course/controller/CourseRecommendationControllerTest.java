package com.todayit.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayit.common.exception.BusinessException;
import com.todayit.common.exception.GlobalExceptionHandler;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.CourseFacade;
import com.todayit.course.service.CourseRecommendationFacade;
import com.todayit.course.service.command.CourseRecommendationCommand;
import com.todayit.member.exception.MemberErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CourseRecommendationControllerTest {
  @Mock private CourseRecommendationFacade facade;
  @Mock private CourseFacade courseFacade;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(new CourseController(courseFacade, facade))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("반복 전달된 themes 쿼리 파라미터를 바인딩하고 인증된 회원 기준으로 추천 코스를 조회한다")
  void bindsRepeatedThemesAndUsesAuthenticatedMember() throws Exception {
    // given
    when(facade.recommend(eq("member"), any())).thenReturn(List.of());

    // when
    ResultActions result =
        mvc.perform(
            get("/api/v1/courses/recommendations")
                .principal(new UsernamePasswordAuthenticationToken("member", null))
                .param("region", "11110")
                .param("themes", "감성적", "조용함")
                .param("transport", "WALK")
                .param("durationMinutes", "120"));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.courses").isEmpty());
    verify(facade)
        .recommend(
            "member",
            new CourseRecommendationCommand(
                "11110", List.of("감성적", "조용함"), 120, CourseTransport.WALKING));
  }

  @Test
  @DisplayName("유효하지 않은 추천 조회 조건이면 파사드 호출 전에 400 Bad Request를 반환한다")
  void rejectsInvalidQueryBeforeCallingFacade() throws Exception {
    // given
    UsernamePasswordAuthenticationToken principal =
        new UsernamePasswordAuthenticationToken("member", null);

    // when
    ResultActions result =
        mvc.perform(
            get("/api/v1/courses/recommendations")
                .principal(principal)
                .param("durationMinutes", "0"));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.message").value("예상시간 상한은 양수이어야 합니다."));
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("추천 요청 회원을 찾을 수 없으면 404 Not Found를 반환한다")
  void returnsMemberNotFoundError() throws Exception {
    // given
    when(facade.recommend(eq("member"), any()))
        .thenThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

    // when
    ResultActions result =
        mvc.perform(
            get("/api/v1/courses/recommendations")
                .principal(new UsernamePasswordAuthenticationToken("member", null)));

    // then
    result
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
  }
}

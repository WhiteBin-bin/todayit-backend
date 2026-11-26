package com.todayit.course.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayit.common.auth.config.SecurityConfig;
import com.todayit.common.auth.filter.JwtAuthenticationFilter;
import com.todayit.common.auth.handler.RestAccessDeniedHandler;
import com.todayit.common.auth.handler.RestAuthenticationEntryPoint;
import com.todayit.common.auth.jwt.JwtTokenProvider;
import com.todayit.common.auth.token.RefreshTokenService;
import com.todayit.course.service.CourseFacade;
import com.todayit.course.service.CourseRecommendationFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(
    controllers = CourseController.class,
    properties = {
      "todayit.security.jwt.secret=01234567890123456789012345678901",
      "todayit.security.jwt.access-token-expiration=30m"
    })
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("코스 API 인증 및 인가 보안 테스트")
class CourseSecurityTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private CourseFacade facade;
  @MockitoBean private CourseRecommendationFacade recommendations;
  @MockitoBean private JwtTokenProvider tokens;
  @MockitoBean private RefreshTokenService sessions;

  @Test
  @DisplayName("추천 코스는 공개 상세 경로와 구분하여 비회원 요청을 거부한다")
  void rejectsAnonymousRecommendations() throws Exception {
    // given
    String path = "/api/v1/courses/recommendations";

    // when
    ResultActions result = mvc.perform(get(path));

    // then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
        .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    verifyNoInteractions(recommendations);
  }

  @Test
  @DisplayName("코스 생성은 비회원 요청을 저장 전에 거부한다")
  void rejectsAnonymousCreation() throws Exception {
    // given
    String request = "{}";

    // when
    ResultActions result =
        mvc.perform(
            post("/api/v1/courses").contentType(MediaType.APPLICATION_JSON).content(request));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("코스 장소 순서 변경은 비회원 요청을 거부한다")
  void rejectsAnonymousPlaceReorder() throws Exception {
    // given
    String request = "{\"coursePlaceIds\":[1,2]}";

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/places/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("코스 장소 삭제는 비회원 요청을 거부한다")
  void rejectsAnonymousPlaceDeletion() throws Exception {
    // given
    String path = "/api/v1/courses/1/places/10";

    // when
    ResultActions result = mvc.perform(delete(path));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("코스 삭제는 비회원 요청을 거부한다")
  void rejectsAnonymousCourseDeletion() throws Exception {
    // given
    String path = "/api/v1/courses/1";

    // when
    ResultActions result = mvc.perform(delete(path));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("코스 복사는 비회원 요청을 거부한다")
  void rejectsAnonymousCourseCopy() throws Exception {
    // given
    String path = "/api/v1/courses/1/copy";

    // when
    ResultActions result = mvc.perform(post(path));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("코스 생성 조건 수정은 비회원 요청을 거부한다")
  void rejectsAnonymousCourseConditionsUpdate() throws Exception {
    // given
    String request = "{\"transport\":\"CAR\"}";

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/conditions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }

  @Test
  @DisplayName("코스 이동수단 변경은 비회원 요청을 거부한다")
  void rejectsAnonymousCourseTransportUpdate() throws Exception {
    // given
    String request = "{\"transport\":\"CAR\"}";

    // when
    ResultActions result =
        mvc.perform(
            patch("/api/v1/courses/1/transport")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));

    // then
    result.andExpect(status().isUnauthorized());
    verifyNoInteractions(facade);
  }
}

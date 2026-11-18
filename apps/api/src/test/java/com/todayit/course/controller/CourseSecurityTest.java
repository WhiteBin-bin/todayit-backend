package com.todayit.course.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayit.common.auth.config.SecurityConfig;
import com.todayit.common.auth.filter.JwtAuthenticationFilter;
import com.todayit.common.auth.handler.RestAccessDeniedHandler;
import com.todayit.common.auth.handler.RestAuthenticationEntryPoint;
import com.todayit.common.auth.jwt.JwtTokenProvider;
import com.todayit.common.auth.token.RefreshTokenService;
import com.todayit.course.service.CourseFacade;
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
@DisplayName("코스 생성 인증")
class CourseSecurityTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private CourseFacade facade;
  @MockitoBean private JwtTokenProvider tokens;
  @MockitoBean private RefreshTokenService sessions;

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
}

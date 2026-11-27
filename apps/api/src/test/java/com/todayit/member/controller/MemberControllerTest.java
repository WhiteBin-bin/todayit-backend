package com.todayit.member.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayit.common.exception.BusinessException;
import com.todayit.common.exception.GlobalExceptionHandler;
import com.todayit.common.exception.InvalidRequestException;
import com.todayit.common.pagination.PageResult;
import com.todayit.course.entity.CourseStatus;
import com.todayit.course.entity.CourseVisibility;
import com.todayit.course.service.MemberCourseQueryService;
import com.todayit.course.service.model.CourseScrapSort;
import com.todayit.course.service.model.MyCourseCardResult;
import com.todayit.course.service.model.MyCourseSort;
import com.todayit.member.entity.PreferredRegionCode;
import com.todayit.member.exception.MemberErrorCode;
import com.todayit.member.service.MemberProfileFacade;
import com.todayit.member.service.MemberProfileService;
import com.todayit.member.service.SignupService;
import com.todayit.member.service.command.MemberProfileCommand;
import com.todayit.member.service.model.MemberProfileDetailsResult;
import com.todayit.member.service.model.MemberProfileResult;
import com.todayit.place.entity.Category;
import com.todayit.place.service.PlaceScrapQueryService;
import com.todayit.place.service.model.PlaceResult;
import com.todayit.place.service.model.PlaceScrapSort;
import com.todayit.theme.Theme;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

  @Mock private PlaceScrapQueryService placeScrapQueryService;
  @Mock private SignupService signupService;
  @Mock private MemberProfileFacade memberProfileFacade;
  @Mock private MemberProfileService memberProfileService;
  @Mock private MemberCourseQueryService memberCourseQueryService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new MemberController(
                    signupService,
                    placeScrapQueryService,
                    memberProfileFacade,
                    memberProfileService,
                    memberCourseQueryService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("회원이 스크랩한 장소를 조회한다")
  void returnsScrappedPlaces() throws Exception {
    // given
    PlaceResult place =
        new PlaceResult(
            1,
            "오늘의 식당",
            new BigDecimal("37.5"),
            new BigDecimal("127.0"),
            "서울특별시 종로구 종로 1",
            Category.RESTAURANT,
            10,
            List.of("https://example.com/place.jpg"),
            false);
    when(placeScrapQueryService.findScrappedPlaces("member-1", 1, 20, PlaceScrapSort.OLDEST))
        .thenReturn(new PageResult<>(List.of(place), 1, 20, 21));

    // when
    ResultActions result =
        mockMvc.perform(
            get("/api/v1/members/me/scrapped-places")
                .param("page", "1")
                .param("sort", "OLDEST")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null)));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content[0].placeId").value(1))
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.size").value(20))
        .andExpect(jsonPath("$.data.totalElements").value(21))
        .andExpect(jsonPath("$.data.totalPages").value(2));
    verify(placeScrapQueryService).findScrappedPlaces("member-1", 1, 20, PlaceScrapSort.OLDEST);
  }

  @Test
  @DisplayName("이미 사용 중인 닉네임이면 available false를 반환한다")
  void returnsAvailableFalse() throws Exception {
    // given
    String nickname = "더미사용자";
    when(signupService.isNicknameAvailable(nickname)).thenReturn(false);

    // when
    ResultActions result =
        mockMvc.perform(get("/api/v1/members/nickname/check").param("nickname", nickname));

    // then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.available").value(false));
    verify(signupService).isNicknameAvailable(nickname);
  }

  @Test
  @DisplayName("닉네임 파라미터가 없으면 400을 반환한다")
  void rejectsMissingNickname() throws Exception {
    // given
    when(signupService.isNicknameAvailable(null))
        .thenThrow(new InvalidRequestException("닉네임은 필수입니다."));

    // when
    ResultActions result = mockMvc.perform(get("/api/v1/members/nickname/check"));

    // then
    result
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    verify(signupService).isNicknameAvailable(null);
  }

  @Test
  @DisplayName("프로필 이미지를 업로드하면 변경된 회원 정보를 반환한다")
  void uploadsProfileImage() throws Exception {
    // given
    String memberId = "member-1";
    String profileImageUrl = "https://example.com/profile.png";
    LocalDateTime updatedAt = LocalDateTime.of(2026, 11, 4, 5, 0);

    MockMultipartFile file =
        new MockMultipartFile("file", "profile.png", "image/png", new byte[] {1, 2, 3});

    MemberProfileResult result = new MemberProfileResult(memberId, profileImageUrl, updatedAt);

    when(memberProfileFacade.uploadProfileImage(eq(memberId), any(MultipartFile.class)))
        .thenReturn(result);

    // when
    ResultActions response =
        mockMvc.perform(
            multipart("/api/v1/members/me/profile-image")
                .file(file)
                .principal(new UsernamePasswordAuthenticationToken(memberId, null)));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.memberId").value(memberId))
        .andExpect(jsonPath("$.data.profileImage").value(profileImageUrl))
        .andExpect(jsonPath("$.data.updatedAt").value("2026-11-04T05:00:00"));
    verify(memberProfileFacade).uploadProfileImage(eq(memberId), any(MultipartFile.class));
  }

  @Test
  @DisplayName("프로필 이미지 약관에 동의하지 않으면 400을 반환한다")
  void rejectsProfileImageWithoutConsent() throws Exception {
    // given
    String memberId = "member-1";

    MockMultipartFile file =
        new MockMultipartFile("file", "profile.png", "image/png", new byte[] {1, 2, 3});

    when(memberProfileFacade.uploadProfileImage(eq(memberId), any(MultipartFile.class)))
        .thenThrow(new BusinessException(MemberErrorCode.PROFILE_IMAGE_CONSENT_REQUIRED));

    // when
    ResultActions response =
        mockMvc.perform(
            multipart("/api/v1/members/me/profile-image")
                .file(file)
                .principal(new UsernamePasswordAuthenticationToken(memberId, null)));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("PROFILE_IMAGE_CONSENT_REQUIRED"));
  }

  @Test
  @DisplayName("프로필 이미지 파일이 없으면 400을 반환한다")
  void rejectsMissingProfileImage() throws Exception {
    // given
    UsernamePasswordAuthenticationToken principal =
        new UsernamePasswordAuthenticationToken("member-1", null);

    // when
    ResultActions response =
        mockMvc.perform(multipart("/api/v1/members/me/profile-image").principal(principal));

    // then
    response.andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원 프로필 수정 요청을 처리하고 변경 결과를 반환한다")
  void updatesMemberProfile() throws Exception {
    // given
    String memberId = "member-1";
    LocalDateTime updatedAt = LocalDateTime.of(2026, 11, 9, 4, 0);

    MemberProfileDetailsResult result =
        new MemberProfileDetailsResult(
            memberId,
            "test@example.com",
            "새닉네임",
            null,
            List.of(new PreferredRegionCode("11", "110")),
            List.of(Theme.QUIET, Theme.ROMANTIC),
            updatedAt);

    when(memberProfileService.updateProfile(eq(memberId), any(MemberProfileCommand.class)))
        .thenReturn(result);

    // when
    ResultActions response =
        mockMvc.perform(
            patch("/api/v1/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                            {
                              "nickname": "새닉네임",
                              "preferredRegions": [
                                {
                                  "sidoCode": "11",
                                  "sigunguCode": "110"
                                }
                              ],
                              "preferredThemes": ["QUIET", "ROMANTIC"]
                            }
                            """)
                .principal(new UsernamePasswordAuthenticationToken(memberId, null)));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.memberId").value(memberId))
        .andExpect(jsonPath("$.data.nickname").value("새닉네임"))
        .andExpect(jsonPath("$.data.preferredRegions[0].sidoCode").value("11"))
        .andExpect(jsonPath("$.data.preferredRegions[0].sigunguCode").value("110"))
        .andExpect(jsonPath("$.data.preferredThemes[0]").value("QUIET"))
        .andExpect(jsonPath("$.data.preferredThemes[1]").value("ROMANTIC"))
        .andExpect(jsonPath("$.data.updatedAt").value("2026-11-09T04:00:00"));

    verify(memberProfileService)
        .updateProfile(
            eq(memberId),
            eq(
                new MemberProfileCommand(
                    "새닉네임",
                    List.of(new PreferredRegionCode("11", "110")),
                    List.of(Theme.QUIET, Theme.ROMANTIC))));
  }

  @Test
  @DisplayName("선호정보 약관에 동의하지 않으면 400을 반환한다")
  void rejectsProfileUpdateWithoutPreferenceConsent() throws Exception {
    // given
    String memberId = "member-1";

    when(memberProfileService.updateProfile(eq(memberId), any(MemberProfileCommand.class)))
        .thenThrow(new BusinessException(MemberErrorCode.PREFERENCE_CONSENT_REQUIRED));

    // when
    ResultActions response =
        mockMvc.perform(
            patch("/api/v1/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                      "preferredThemes": ["QUIET"]
                                    }
                                    """)
                .principal(new UsernamePasswordAuthenticationToken(memberId, null)));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("PREFERENCE_CONSENT_REQUIRED"))
        .andExpect(jsonPath("$.message").value("선호정보 저장을 위한 약관 동의가 필요합니다."));
  }

  @Test
  @DisplayName("수정 항목이 없으면 400을 반환한다")
  void rejectsEmptyMemberProfileRequest() throws Exception {
    // given
    UsernamePasswordAuthenticationToken principal =
        new UsernamePasswordAuthenticationToken("member-1", null);

    // when
    ResultActions response =
        mockMvc.perform(
            patch("/api/v1/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .principal(principal));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  @DisplayName("잘못된 테마 코드를 전달하면 400을 반환한다")
  void rejectsUnknownTheme() throws Exception {
    // given
    UsernamePasswordAuthenticationToken principal =
        new UsernamePasswordAuthenticationToken("member-1", null);

    // when
    ResultActions response =
        mockMvc.perform(
            patch("/api/v1/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"preferredThemes\":[\"UNKNOWN_THEME\"]}")
                .principal(principal));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  @DisplayName("로그인한 회원 본인의 프로필 정보를 반환한다")
  void findsMemberProfile() throws Exception {
    // given
    String memberId = "member-1";
    LocalDateTime updatedAt = LocalDateTime.of(2026, 11, 9, 4, 0);

    MemberProfileDetailsResult result =
        new MemberProfileDetailsResult(
            memberId,
            "test@example.com",
            "테스트회원",
            null,
            List.of(new PreferredRegionCode("11", "110")),
            List.of(Theme.QUIET),
            updatedAt);

    when(memberProfileService.findProfile(memberId)).thenReturn(result);

    // when
    ResultActions response =
        mockMvc.perform(
            get("/api/v1/members/me")
                .principal(new UsernamePasswordAuthenticationToken(memberId, null)));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.memberId").value(memberId))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.nickname").value("테스트회원"))
        .andExpect(jsonPath("$.data.preferredRegions[0].sidoCode").value("11"))
        .andExpect(jsonPath("$.data.preferredRegions[0].sigunguCode").value("110"))
        .andExpect(jsonPath("$.data.preferredThemes[0]").value("QUIET"));
    verify(memberProfileService).findProfile(memberId);
  }

  @Test
  @DisplayName("프로필 이미지를 삭제하면 기본 이미지 상태를 반환한다")
  void deletesProfileImage() throws Exception {
    // given
    String memberId = "member-1";
    LocalDateTime updatedAt = LocalDateTime.of(2026, 11, 10, 10, 0);

    MemberProfileResult result = new MemberProfileResult(memberId, null, updatedAt);

    when(memberProfileFacade.deleteProfileImage(memberId)).thenReturn(result);

    // when
    ResultActions response =
        mockMvc.perform(
            delete("/api/v1/members/me/profile-image")
                .principal(new UsernamePasswordAuthenticationToken(memberId, null)));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.memberId").value(memberId))
        .andExpect(jsonPath("$.data.profileImage").value(nullValue()))
        .andExpect(jsonPath("$.data.updatedAt").value("2026-11-10T10:00:00"));
    verify(memberProfileFacade).deleteProfileImage(memberId);
  }

  @Test
  @DisplayName("선호 지역을 2개 선택하면 400을 반환한다")
  void rejectsMultiplePreferredRegions() throws Exception {
    // given
    UsernamePasswordAuthenticationToken principal =
        new UsernamePasswordAuthenticationToken("member-1", null);

    // when
    ResultActions response =
        mockMvc.perform(
            patch("/api/v1/members/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                      "preferredRegions": [
                                        {
                                          "sidoCode": "11",
                                          "sigunguCode": "110"
                                        },
                                        {
                                          "sidoCode": "41",
                                          "sigunguCode": "135"
                                        }
                                      ]
                                    }
                                    """)
                .principal(principal));

    // then
    response
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  @DisplayName("회원이 작성한 코스 목록을 조회한다")
  void returnsMyCoursesSuccessfully() throws Exception {
    // given
    MyCourseCardResult card =
        new MyCourseCardResult(
            1,
            "데이트 코스",
            CourseStatus.ACTIVE,
            CourseVisibility.PUBLIC,
            "https://example.com/place.jpg",
            2,
            "서울특별시",
            "종로구",
            List.of("카페·디저트"),
            5L,
            LocalDateTime.of(2026, 11, 24, 10, 0),
            LocalDateTime.of(2026, 11, 24, 18, 0),
            LocalDateTime.of(2026, 11, 24, 9, 0),
            LocalDateTime.of(2026, 11, 24, 12, 0));
    PageResult<MyCourseCardResult> pageResult = new PageResult<>(List.of(card), 0, 20, 1L);

    when(memberCourseQueryService.findMyCourses(
            "member-1", CourseStatus.ACTIVE, 0, 20, MyCourseSort.LATEST))
        .thenReturn(pageResult);

    // when
    ResultActions response =
        mockMvc.perform(
            get("/api/v1/members/me/courses")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("sort", "LATEST")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null)));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].courseId").value(1))
        .andExpect(jsonPath("$.data.content[0].title").value("데이트 코스"))
        .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.data.content[0].visibility").value("PUBLIC"))
        .andExpect(
            jsonPath("$.data.content[0].thumbnailUrl").value("https://example.com/place.jpg"))
        .andExpect(jsonPath("$.data.content[0].placeCount").value(2))
        .andExpect(jsonPath("$.data.content[0].sidoName").value("서울특별시"))
        .andExpect(jsonPath("$.data.content[0].sigunguName").value("종로구"))
        .andExpect(jsonPath("$.data.content[0].likeCount").value(5))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.totalPages").value(1));

    verify(memberCourseQueryService)
        .findMyCourses("member-1", CourseStatus.ACTIVE, 0, 20, MyCourseSort.LATEST);
  }

  @Test
  @DisplayName("회원이 스크랩한 코스 목록을 조회한다")
  void returnsScrappedCoursesSuccessfully() throws Exception {
    // given
    MyCourseCardResult card =
        new MyCourseCardResult(
            1,
            "스크랩 데이트 코스",
            CourseStatus.ACTIVE,
            CourseVisibility.PUBLIC,
            "https://example.com/place.jpg",
            2,
            "서울특별시",
            "종로구",
            List.of("카페·디저트"),
            5L,
            LocalDateTime.of(2026, 11, 24, 10, 0),
            LocalDateTime.of(2026, 11, 24, 18, 0),
            LocalDateTime.of(2026, 11, 24, 9, 0),
            LocalDateTime.of(2026, 11, 24, 12, 0));
    PageResult<MyCourseCardResult> pageResult = new PageResult<>(List.of(card), 0, 20, 1L);

    when(memberCourseQueryService.findScrappedCourses("member-1", 0, 20, CourseScrapSort.LATEST))
        .thenReturn(pageResult);

    // when
    ResultActions response =
        mockMvc.perform(
            get("/api/v1/members/me/scrapped-courses")
                .param("page", "0")
                .param("sort", "LATEST")
                .principal(new UsernamePasswordAuthenticationToken("member-1", null)));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].courseId").value(1))
        .andExpect(jsonPath("$.data.content[0].title").value("스크랩 데이트 코스"))
        .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$.data.content[0].visibility").value("PUBLIC"))
        .andExpect(
            jsonPath("$.data.content[0].thumbnailUrl").value("https://example.com/place.jpg"))
        .andExpect(jsonPath("$.data.content[0].placeCount").value(2))
        .andExpect(jsonPath("$.data.content[0].sidoName").value("서울특별시"))
        .andExpect(jsonPath("$.data.content[0].sigunguName").value("종로구"))
        .andExpect(jsonPath("$.data.content[0].likeCount").value(5))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.totalPages").value(1));

    verify(memberCourseQueryService).findScrappedCourses("member-1", 0, 20, CourseScrapSort.LATEST);
  }
}

package com.todayit.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.todayit.place.service.model.PlaceDetailWithStayResult;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** 상세 조회의 PostgreSQL 저장 계약을 검증합니다. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlaceDetailIntegrationTest {

  private static final Logger LOG = LoggerFactory.getLogger(PlaceDetailIntegrationTest.class);

  @BeforeEach
  void logTestStart(TestInfo testInfo) {
    LOG.info("테스트 시작: {}", testInfo.getDisplayName());
  }

  @AfterEach
  void logTestEnd(TestInfo testInfo) {
    LOG.info("테스트 종료: {}", testInfo.getDisplayName());
  }

  @Autowired JdbcTemplate jdbc;
  @Autowired PlaceDetailFacade facade;

  private int createPlace() {
    return jdbc.queryForObject(
        """
        INSERT INTO place(name, latitude, longitude, address, category, is_active, is_deleted, description)
        VALUES ('상세 테스트 장소', 37.5, 127.0, '서울', 'CAFE_DESSERT', true, false, '장소 설명')
        RETURNING place_id
        """,
        Integer.class);
  }

  @Test
  @DisplayName("관련 데이터가 없는 장소는 평균 0분과 빈 영업시간 및 이미지 목록을 반환한다")
  void returnsZeroAndEmptyListsWhenNoRelatedRecordsExist() {
    // Given
    int placeId = createPlace();

    // When
    PlaceDetailWithStayResult result = facade.getPlaceDetail(placeId, null);

    // Then
    assertThat(result.averageStayMinutes()).isZero();
    assertThat(result.detail().description()).isEqualTo("장소 설명");
    assertThat(result.detail().openingHours()).isEmpty();
    assertThat(result.detail().place().imageUrls()).isEmpty();
    assertThat(result.detail().liked()).isFalse();
    assertThat(result.detail().scrapped()).isFalse();
  }

  @Test
  @DisplayName("모든 코스에서 해당 장소의 체류 시간만 평균 내고 회원 상태를 반환한다")
  void averagesOnlyMatchingPlaceAcrossAllCoursesAndReturnsMemberStates() {
    // Given
    int placeId = createPlace();
    int otherPlaceId = createPlace();
    String memberId = "detail-test-member";
    jdbc.update(
        "INSERT INTO member(member_id, provider, nickname) VALUES (?, 'GOOGLE', '상세 테스트 회원')",
        memberId);
    int order = 0;
    for (String status : List.of("DRAFT", "ACTIVE", "DELETED")) {
      int courseId =
          jdbc.queryForObject(
              """
          INSERT INTO course(member_id,title,transport,start_at,end_at,status,visibility)
          VALUES (?, '테스트 코스', 'WALKING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, 'PRIVATE')
          RETURNING course_id
          """,
              Integer.class,
              memberId,
              status);
      jdbc.update(
          "INSERT INTO course_place(course_id, place_id, stay_minute) VALUES (?, ?, ?)",
          courseId,
          placeId,
          30 + order++);
      jdbc.update(
          "INSERT INTO course_place(course_id, place_id, stay_minute, \"order\") VALUES (?, ?, 999, 1)",
          courseId,
          otherPlaceId);
    }
    jdbc.update(
        "INSERT INTO place_member_like(member_id,place_id) VALUES (?,?)", memberId, placeId);
    jdbc.update(
        "INSERT INTO place_scrap(member_id,place_id,is_deleted) VALUES (?,?,false)",
        memberId,
        placeId);
    jdbc.update(
        "INSERT INTO place_image(place_id,image_url) VALUES (?,?)",
        placeId,
        "https://example.com/place.jpg");
    jdbc.update(
        """
        INSERT INTO hours(place_id,open_date,start_at,end_at)
        VALUES (?, '2026-10-16 00:00:00+09', '2026-10-16 09:00:00+09', '2026-10-16 18:00:00+09')
        """,
        placeId);

    // When
    PlaceDetailWithStayResult result = facade.getPlaceDetail(placeId, memberId);

    // Then
    assertThat(result.averageStayMinutes()).isEqualTo(31);
    assertThat(result.detail().openingHours()).hasSize(1);
    assertThat(result.detail().place().imageUrls())
        .containsExactly("https://example.com/place.jpg");
    assertThat(result.detail().liked()).isTrue();
    assertThat(result.detail().scrapped()).isTrue();
  }

  @Test
  @DisplayName("취소된 스크랩은 장소 상세에서 false로 반환한다")
  void returnsFalseForCancelledScrap() {
    // Given
    int placeId = createPlace();
    String memberId = "cancelled-scrap-member";
    jdbc.update(
        "INSERT INTO member(member_id, provider, nickname) VALUES (?, 'GOOGLE', '취소 스크랩 테스트 회원')",
        memberId);
    jdbc.update(
        "INSERT INTO place_scrap(member_id,place_id,is_deleted) VALUES (?,?,true)",
        memberId,
        placeId);

    // When
    PlaceDetailWithStayResult result = facade.getPlaceDetail(placeId, memberId);

    // Then
    assertThat(result.detail().scrapped()).isFalse();
  }
}

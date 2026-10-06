package com.todayit.place.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todayit.place.entity.Category;
import com.todayit.place.entity.Place;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/todayit?currentSchema=todayit",
      "spring.datasource.username=todayit_user",
      "spring.datasource.password=todayit1234",
      "spring.flyway.enabled=true",
      "spring.flyway.locations=classpath:db/migration",
      "spring.jpa.hibernate.ddl-auto=none"
    })
class PlaceRepositoryTest {

  @Autowired private PlaceRepository placeRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("DELETE FROM place_member_like");
    jdbcTemplate.update("DELETE FROM place_scrap");
    jdbcTemplate.update("DELETE FROM region_place");
    jdbcTemplate.update("DELETE FROM hours");
    jdbcTemplate.update("DELETE FROM place_image");
    jdbcTemplate.update("DELETE FROM place");
  }

  @Test
  @DisplayName("활성화되고 삭제되지 않은 장소만 페이지 단위로 조회한다")
  @Transactional
  void findsActiveAndUndeletedPlaces() {
    // Given
    jdbcTemplate.update(
        """
        INSERT INTO place (
            name, latitude, longitude, address, category, view_count, is_active, is_deleted
        ) VALUES
            ('활성 장소', 37.50000000, 127.00000000, '주소 1', 'RESTAURANT', 10, TRUE, FALSE),
            ('비활성 장소', 37.50000000, 127.00000000, '주소 2', 'CAFE_DESSERT', 20, FALSE, FALSE),
            ('삭제 장소', 37.50000000, 127.00000000, '주소 3', 'BAR', 30, TRUE, TRUE)
        """);

    // When
    Page<Place> result =
        placeRepository.findPlaces(
            List.of(0), false, List.of(Category.values()), false, false, 0, PageRequest.of(0, 10));

    // Then
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().getSnapshot().name()).isEqualTo("활성 장소");
  }

  @Test
  @DisplayName("지역, 다중 카테고리, 영업 요일이 모두 일치하는 장소만 조회한다")
  @Transactional
  void findsPlacesMatchingAllFilters() {
    // Given
    Integer regionId =
        jdbcTemplate.queryForObject(
            "INSERT INTO region (si, gun, gu) VALUES ('서울특별시', '해당 없음', '종로구') RETURNING region_id",
            Integer.class);
    Integer restaurantId = insertPlace("월요일 식당", "RESTAURANT");
    Integer cafeId = insertPlace("화요일 카페", "CAFE_DESSERT");
    Integer barId = insertPlace("월요일 바", "BAR");
    linkRegion(regionId, restaurantId);
    linkRegion(regionId, cafeId);
    linkRegion(regionId, barId);
    insertHours(restaurantId, "2026-10-05T09:00:00");
    insertHours(cafeId, "2026-10-06T09:00:00");
    insertHours(barId, "2026-10-05T18:00:00");

    // When
    Page<Place> result =
        placeRepository.findPlaces(
            List.of(regionId),
            true,
            List.of(Category.RESTAURANT, Category.CAFE_DESSERT),
            true,
            true,
            1,
            PageRequest.of(0, 10));

    // Then
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().getFirst().getSnapshot().name()).isEqualTo("월요일 식당");
  }

  @Test
  @DisplayName("상위 지역은 모든 하위 지역을 포함하고 전체 경로는 선택한 하위 지역만 조회한다")
  @Transactional
  void findsPlacesInAllDistrictsOfSelectedCity() {
    // Given
    Integer jongnoRegionId = insertRegion("서울특별시", "해당 없음", "종로구");
    Integer mapoRegionId = insertRegion("서울특별시", "해당 없음", "마포구");
    Integer suwonRegionId = insertRegion("경기도", "수원시", "팔달구");
    Integer jongnoPlaceId = insertPlace("종로 장소", "RESTAURANT");
    Integer mapoPlaceId = insertPlace("마포 장소", "CAFE_DESSERT");
    Integer suwonPlaceId = insertPlace("수원 장소", "BAR");
    linkRegion(jongnoRegionId, jongnoPlaceId);
    linkRegion(mapoRegionId, mapoPlaceId);
    linkRegion(suwonRegionId, suwonPlaceId);

    // When
    Page<Place> result =
        placeRepository.findPlaces(
            List.of(jongnoRegionId, mapoRegionId),
            true,
            List.of(Category.values()),
            false,
            false,
            0,
            PageRequest.of(0, 10));

    // Then
    assertThat(result.getContent())
        .extracting(place -> place.getSnapshot().name())
        .containsExactlyInAnyOrder("종로 장소", "마포 장소");

    Page<Place> districtResult =
        placeRepository.findPlaces(
            List.of(jongnoRegionId),
            true,
            List.of(Category.values()),
            false,
            false,
            0,
            PageRequest.of(0, 10));
    assertThat(districtResult.getContent())
        .extracting(place -> place.getSnapshot().name())
        .containsExactly("종로 장소");
  }

  private Integer insertRegion(String si, String gun, String gu) {
    return jdbcTemplate.queryForObject(
        "INSERT INTO region (si, gun, gu) VALUES (?, ?, ?) RETURNING region_id",
        Integer.class,
        si,
        gun,
        gu);
  }

  private Integer insertPlace(String name, String category) {
    return jdbcTemplate.queryForObject(
        """
        INSERT INTO place (
            name, latitude, longitude, address, category, view_count, is_active, is_deleted
        ) VALUES (?, 37.50000000, 127.00000000, '종로구 주소', ?, 0, TRUE, FALSE)
        RETURNING place_id
        """,
        Integer.class,
        name,
        category);
  }

  private void linkRegion(int regionId, int placeId) {
    jdbcTemplate.update(
        "INSERT INTO region_place (region_id, place_id) VALUES (?, ?)", regionId, placeId);
  }

  private void insertHours(int placeId, String openDate) {
    jdbcTemplate.update(
        """
        INSERT INTO hours (place_id, open_date, start_at, end_at)
        VALUES (?, CAST(? AS TIMESTAMP WITH TIME ZONE),
                CAST(? AS TIMESTAMP WITH TIME ZONE),
                CAST(? AS TIMESTAMP WITH TIME ZONE) + INTERVAL '1 hour')
        """,
        placeId,
        openDate,
        openDate,
        openDate);
  }
}

package com.todayit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:local_seed;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password="
    })
class FlywayLocalSeedTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void seedsOneDummyMemberForEachRole() {
    List<String> roleNames =
        jdbcTemplate.queryForList(
            """
            SELECT roles.name
            FROM member_roles
            JOIN roles ON roles.roles_id = member_roles.roles_id
            JOIN member ON member.member_id = member_roles.member_id
            WHERE member.email IN (
                'user@todayit.local',
                'admin@todayit.local',
                'dev@todayit.local'
            )
            ORDER BY roles.name
            """,
            String.class);

    assertThat(roleNames).containsExactly("ADMIN", "DEV", "USER");
  }

  @Test
  void seedsNationwideRegionsAndConnectsEveryDummyPlace() {
    Integer regionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM region", Integer.class);
    Integer unmappedPlaceCount =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM place
            LEFT JOIN region_place ON region_place.place_id = place.place_id
            WHERE region_place.region_place_id IS NULL
            """,
            Integer.class);

    assertThat(regionCount).isEqualTo(256);
    assertThat(unmappedPlaceCount).isZero();
    assertThat(hasRegion("서울특별시", "해당 없음", "종로구")).isTrue();
    assertThat(hasRegion("경기도", "수원시", "팔달구")).isTrue();
    assertThat(hasRegion("세종특별자치시", "해당 없음", "해당 없음")).isTrue();
  }

  private boolean hasRegion(String si, String gun, String gu) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM region WHERE si = ? AND gun = ? AND gu = ?",
            Integer.class,
            si,
            gun,
            gu);
    return count != null && count > 0;
  }
}

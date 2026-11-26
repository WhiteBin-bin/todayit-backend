package com.todayit.course.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todayit.common.exception.InvalidRequestException;
import com.todayit.course.entity.CourseTransport;
import com.todayit.course.service.command.CourseRecommendationCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CourseRecommendationRequest 단위 테스트")
class CourseRecommendationRequestTest {
  @ParameterizedTest
  @ValueSource(strings = {"WALK", "WALKING"})
  @DisplayName("도보 이동수단 값(WALK, WALKING)을 WALKING으로 변환한다")
  void acceptsWalkingAliases(String transport) {
    // given
    CourseRecommendationRequest request =
        new CourseRecommendationRequest("11110", List.of("감성적"), 120, transport);

    // when
    CourseRecommendationCommand command = request.toCommand();

    // then
    assertThat(command.transport()).isEqualTo(CourseTransport.WALKING);
  }

  @Test
  @DisplayName("선택 필터가 모두 생략되면 빈 테마 목록과 null 조건으로 변환한다")
  void acceptsAbsentFilters() {
    // given
    CourseRecommendationRequest request = new CourseRecommendationRequest(null, null, null, null);

    // when
    CourseRecommendationCommand command = request.toCommand();

    // then
    assertThat(command.themes()).isEmpty();
    assertThat(command.transport()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "110", "서울", "111100"})
  @DisplayName("유효하지 않은 지역 코드 형식이면 예외를 던진다")
  void rejectsInvalidRegion(String region) {
    // given
    CourseRecommendationRequest request = new CourseRecommendationRequest(region, null, null, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception).isNotNull();
  }

  @Test
  @DisplayName("테마가 중복되거나 2개를 초과하면 예외를 던진다")
  void rejectsDuplicateOrTooManyConcepts() {
    // given
    CourseRecommendationRequest duplicateThemesRequest =
        new CourseRecommendationRequest(null, List.of("감성적", "감성적"), null, null);
    CourseRecommendationRequest tooManyThemesRequest =
        new CourseRecommendationRequest(null, List.of("a", "b", "c"), null, null);

    // when
    InvalidRequestException duplicateException =
        assertThrows(InvalidRequestException.class, duplicateThemesRequest::toCommand);
    InvalidRequestException tooManyException =
        assertThrows(InvalidRequestException.class, tooManyThemesRequest::toCommand);

    // then
    assertThat(duplicateException).isNotNull();
    assertThat(tooManyException).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  @DisplayName("예상시간 상한이 0 이하이면 예외를 던진다")
  void rejectsNonPositiveTime(int time) {
    // given
    CourseRecommendationRequest request = new CourseRecommendationRequest(null, null, time, null);

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception).isNotNull();
  }

  @Test
  @DisplayName("허용되지 않은 이동수단 값(예: BUS_FIRST)이면 예외를 던진다")
  void rejectsUnknownTransport() {
    // given
    CourseRecommendationRequest request =
        new CourseRecommendationRequest(null, null, null, "BUS_FIRST");

    // when
    InvalidRequestException exception =
        assertThrows(InvalidRequestException.class, request::toCommand);

    // then
    assertThat(exception).isNotNull();
  }
}

package com.todayit.course.dto.request;

import com.todayit.course.entity.CourseTransport;
import com.todayit.course.exception.CourseErrorCode;
import com.todayit.course.exception.CourseException;
import com.todayit.course.service.command.CreateCourseCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * 코스 생성 요청입니다.
 *
 * @param title 코스명, 앞뒤 공백 제거 후 1~100자
 * @param themes 중복 없는 콘셉트 1~2개
 * @param startAt 방문 시작 시각
 * @param endAt 방문 종료 시각, 선택 입력
 * @param transport 이동수단
 */
@Schema(
    description =
        """
        새 코스를 생성할 때 입력하는 정보입니다.

        - `title`, `themes`, `startAt`, `transport`는 필수입니다.
        - `endAt`은 선택이며 입력하지 않으면 `null`로 저장됩니다.
        """)
public record CreateCourseRequest(
    @Schema(
            description =
                """
                사용자가 정한 코스 이름입니다.

                - 앞뒤 공백을 제거한 뒤 1~100자로 입력합니다.
                """,
            example = "종로 데이트",
            minLength = 1,
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
    @Schema(
            description =
                """
                코스에서 느끼고 싶은 콘셉트 이름 목록입니다.

                - 중복 없이 1~2개 입력합니다.
                - 허용 콘셉트: `조용함`, `로맨틱`, `아늑함`, `감성적`, `활기참`, `이색적`, `여유로움`, `고급스러움`, `레트로`, `자연친화적`
                """,
            example = "[\"감성적\", \"여유로움\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> themes,
    @Schema(
            description =
                """
                코스 방문 시작 일시입니다.

                - `2026-12-01T10:00:00` 형식(ISO 8601)으로 입력합니다.
                """,
            example = "2026-12-01T10:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime startAt,
    @Schema(
            description =
                """
                코스 방문 종료 일시입니다.

                - 선택 항목이며 입력하는 경우 시작 일시(`startAt`)보다 이후여야 합니다.
                - `2026-12-01T14:00:00` 형식(ISO 8601)으로 입력합니다.
                """,
            example = "2026-12-01T14:00:00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true)
        LocalDateTime endAt,
    @Schema(
            description =
                """
                코스에서 사용할 이동수단입니다.

                - `CAR`: 자동차
                - `WALKING`: 도보
                - `PUBLIC_TRANSPORT`: 대중교통
                """,
            example = "WALKING",
            requiredMode = Schema.RequiredMode.REQUIRED)
        CourseTransport transport) {

  private static final int MAX_TITLE_LENGTH = 100;
  private static final int MAX_THEME_COUNT = 2;

  /**
   * 필수 입력과 범위, 조건 간 관계를 검증합니다.
   *
   * @throws CourseException 입력 조건이 올바르지 않을 때
   */
  public void validate() {
    if (title == null || title.isBlank() || title.strip().length() > MAX_TITLE_LENGTH) {
      throw new CourseException(CourseErrorCode.INVALID_TITLE);
    }
    if (themes == null
        || themes.isEmpty()
        || themes.size() > MAX_THEME_COUNT
        || themes.stream().anyMatch(theme -> theme == null || theme.isBlank())
        || new HashSet<>(themes).size() != themes.size()) {
      throw new CourseException(CourseErrorCode.INVALID_THEMES);
    }
    if (startAt == null) {
      throw new CourseException(CourseErrorCode.START_AT_REQUIRED);
    }
    if (endAt != null && !endAt.isAfter(startAt)) {
      throw new CourseException(CourseErrorCode.INVALID_END_AT);
    }
    if (transport == null) {
      throw new CourseException(CourseErrorCode.TRANSPORT_REQUIRED);
    }
  }

  /**
   * 검증된 요청을 서비스 입력으로 변환합니다.
   *
   * @return 코스 생성 조건
   */
  public CreateCourseCommand toCommand() {
    return new CreateCourseCommand(title.strip(), themes, startAt, endAt, transport);
  }
}

package com.todayit.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

@DisplayName("OpenAPI 설정 단위 테스트")
class OpenApiConfigTest {

  private final OpenApiConfig config = new OpenApiConfig();

  @Test
  @DisplayName("OpenAPI 빈에 Bearer 인증 스키마가 정상 등록된다")
  void configuresBearerAuthScheme() {
    // when
    OpenAPI openAPI = config.todayItOpenApi();

    // then
    assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey(OpenApiConfig.BEARER_AUTH);
  }

  @Test
  @DisplayName("선택적 인증이 선언된 메서드는 빈 보안 객체({})가 security 목록에 추가된다")
  void addsEmptySecurityItemForOptionalAuth() throws Exception {
    // given
    OperationCustomizer customizer = config.optionalSecurityCustomizer();
    io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
    operation.addSecurityItem(
        new io.swagger.v3.oas.models.security.SecurityRequirement()
            .addList(OpenApiConfig.BEARER_AUTH));

    Method method = TestController.class.getMethod("optionalAuthMethod");
    HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

    // when
    customizer.customize(operation, handlerMethod);

    // then
    assertThat(operation.getSecurity()).hasSize(2);
    boolean hasEmptyItem = operation.getSecurity().stream().anyMatch(Map::isEmpty);
    assertThat(hasEmptyItem).isTrue();
  }

  @Test
  @DisplayName("필수 인증만 선언된 메서드는 빈 보안 객체가 추가되지 않는다")
  void doesNotAddEmptySecurityItemForRequiredAuth() throws Exception {
    // given
    OperationCustomizer customizer = config.optionalSecurityCustomizer();
    io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
    operation.addSecurityItem(
        new io.swagger.v3.oas.models.security.SecurityRequirement()
            .addList(OpenApiConfig.BEARER_AUTH));

    Method method = TestController.class.getMethod("requiredAuthMethod");
    HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

    // when
    customizer.customize(operation, handlerMethod);

    // then
    assertThat(operation.getSecurity()).hasSize(1);
    boolean hasEmptyItem = operation.getSecurity().stream().anyMatch(Map::isEmpty);
    assertThat(hasEmptyItem).isFalse();
  }

  @Test
  @DisplayName("와일드카드 스키마가 application/json 응답으로 정상 병합되고 와일드카드는 제거된다")
  void mergesWildcardSchemaIntoApplicationJson() throws Exception {
    // given
    OperationCustomizer customizer = config.responseSchemaCustomizer();
    io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
    ApiResponses responses = new ApiResponses();
    ApiResponse response = new ApiResponse();
    Content content = new Content();

    MediaType wildcard = new MediaType();
    Schema<?> schema = new Schema<>();
    schema.set$ref("#/components/schemas/TestResponse");
    wildcard.setSchema(schema);

    MediaType json = new MediaType();
    json.setExample("{\"test\": true}");

    content.addMediaType("*/*", wildcard);
    content.addMediaType("application/json", json);
    response.setContent(content);
    responses.addApiResponse("200", response);
    operation.setResponses(responses);

    Method method = TestController.class.getMethod("requiredAuthMethod");
    HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

    // when
    customizer.customize(operation, handlerMethod);

    // then
    assertThat(content).doesNotContainKey("*/*");
    assertThat(content).containsKey("application/json");
    assertThat(content.get("application/json").getSchema()).isNotNull();
    assertThat(content.get("application/json").getSchema().get$ref())
        .isEqualTo("#/components/schemas/TestResponse");
  }

  @Test
  @DisplayName("와일드카드만 있는 응답은 application/json으로 변환된다")
  void convertsWildcardOnlyToApplicationJson() throws Exception {
    // given
    OperationCustomizer customizer = config.responseSchemaCustomizer();
    io.swagger.v3.oas.models.Operation operation = new io.swagger.v3.oas.models.Operation();
    ApiResponses responses = new ApiResponses();
    ApiResponse response = new ApiResponse();
    Content content = new Content();

    MediaType wildcard = new MediaType();
    Schema<?> schema = new Schema<>();
    schema.set$ref("#/components/schemas/TestResponse");
    wildcard.setSchema(schema);

    content.addMediaType("*/*", wildcard);
    response.setContent(content);
    responses.addApiResponse("200", response);
    operation.setResponses(responses);

    Method method = TestController.class.getMethod("requiredAuthMethod");
    HandlerMethod handlerMethod = new HandlerMethod(new TestController(), method);

    // when
    customizer.customize(operation, handlerMethod);

    // then
    assertThat(content).doesNotContainKey("*/*");
    assertThat(content).containsKey("application/json");
    assertThat(content.get("application/json").getSchema()).isNotNull();
    assertThat(content.get("application/json").getSchema().get$ref())
        .isEqualTo("#/components/schemas/TestResponse");
  }

  static class TestController {
    @Operation(
        summary = "선택적 인증 메서드",
        security = {
          @SecurityRequirement(name = ""),
          @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
        })
    public void optionalAuthMethod() {}

    @Operation(
        summary = "필수 인증 메서드",
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public void requiredAuthMethod() {}
  }
}

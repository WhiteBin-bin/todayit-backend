package com.todayit.member.client.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.todayit.member.client.external.KakaoOAuthException.Reason;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class KakaoOAuthClientTest {
  private static final String REDIRECT_URI = "http://localhost:3000/oauth/kakao/callback";

  private KakaoOAuthClient createClient(HttpStatus status, String responseBody) {

    WebClient.Builder builder =
        WebClient.builder()
            .exchangeFunction(
                request ->
                    Mono.just(
                        ClientResponse.create(status)
                            .header("Content-Type", "application/json")
                            .body(responseBody)
                            .build()));

    return new KakaoOAuthClient(builder, "test-rest-api-key", "test-secret", REDIRECT_URI);
  }

  @Test
  @DisplayName("유효한 인가 코드는 Access Token을 반환한다")
  void exchangesAuthorizationCode() {
    // Given
    KakaoOAuthClient client =
        createClient(
            HttpStatus.OK,
            """
                        {
                          "token_type": "bearer",
                          "access_token": "test-access-token"
                        }
                        """);

    // When
    String token = client.exchangeCode("valid-code", REDIRECT_URI);

    // Then
    assertEquals("test-access-token", token);
  }

  @Test
  @DisplayName("잘못된 인가 코드는 인증 실패로 처리한다")
  void rejectsInvalidAuthorizationCode() {
    // Given
    KakaoOAuthClient client = createClient(HttpStatus.BAD_REQUEST, "{}");

    // When
    KakaoOAuthException exception =
        assertThrows(
            KakaoOAuthException.class, () -> client.exchangeCode("invalid-code", REDIRECT_URI));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  @Test
  @DisplayName("요청 제한은 외부 서비스 장애로 처리한다")
  void handlesTooManyRequests() {
    // Given
    KakaoOAuthClient client = createClient(HttpStatus.TOO_MANY_REQUESTS, "{}");

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", REDIRECT_URI));

    // Then
    assertEquals(Reason.PROVIDER_UNAVAILABLE, exception.getReason());
  }

  @Test
  @DisplayName("등록되지 않은 Redirect URI는 거부한다")
  void rejectsInvalidRedirectUri() {
    // Given
    KakaoOAuthClient client = createClient(HttpStatus.OK, "{}");

    // When
    KakaoOAuthException exception =
        assertThrows(
            KakaoOAuthException.class,
            () -> client.exchangeCode("code", "http://localhost:3000/invalid"));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  @Test
  @DisplayName("Access Token이 없는 응답은 거부한다")
  void rejectsMissingAccessToken() {
    // Given
    KakaoOAuthClient client = createClient(HttpStatus.OK, "{\"token_type\":\"bearer\"}");

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", REDIRECT_URI));

    // Then
    assertEquals(Reason.PROVIDER_UNAVAILABLE, exception.getReason());
  }

  @Test
  @DisplayName("정상적인 Access Token으로 사용자 정보를 조회한다")
  void retrievesUserInfo() {
    // Given
    KakaoOAuthClient client =
        createClient(
            HttpStatus.OK,
            """
                    {
                      "id": 123456789,
                      "kakao_account": {
                        "email": "test@example.com",
                        "is_email_verified": true,
                        "is_email_valid": true
                      }
                    }
                    """);

    // When
    KakaoIdentity identity = client.getUserInfo("valid-token");

    // Then
    assertEquals("123456789", identity.providerUserId());
    assertNull(identity.email());
    assertFalse(identity.emailVerified());
  }

  @Test
  @DisplayName("만료된 Access Token은 인증 실패로 처리한다")
  void rejectsInvalidAccessToken() {
    // Given
    KakaoOAuthClient client = createClient(HttpStatus.UNAUTHORIZED, "{}");

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.getUserInfo("invalid-token"));

    // Then
    assertEquals(Reason.INVALID_ACCESS_TOKEN, exception.getReason());
  }

  @Test
  @DisplayName("회원 ID가 없는 응답은 거부한다")
  void rejectsMissingUserId() {
    // Given
    KakaoOAuthClient client =
        createClient(
            HttpStatus.OK,
            """
                    {
                      "kakao_account": {
                        "email": "test@example.com"
                      }
                    }
                    """);

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.getUserInfo("valid-token"));

    // Then
    assertEquals(Reason.INVALID_USER_INFO, exception.getReason());
  }

  @Test
  @DisplayName("Kakao 서버 오류는 외부 서비스 장애로 처리한다")
  void handlesUserInfoServerError() {
    // Given
    KakaoOAuthClient client = createClient(HttpStatus.INTERNAL_SERVER_ERROR, "{}");

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.getUserInfo("valid-token"));

    // Then
    assertEquals(Reason.PROVIDER_UNAVAILABLE, exception.getReason());
  }

  @Test
  @DisplayName("사용자 정보 조회 시 Bearer 토큰을 헤더에 전달한다")
  void sendsBearerTokenToKakao() {
    // Given
    WebClient.Builder builder =
        WebClient.builder()
            .exchangeFunction(
                request -> {
                  // Then: 외부로 전송되는 HTTP 요청 검증
                  assertEquals(HttpMethod.GET, request.method());
                  assertEquals("https://kapi.kakao.com/v2/user/me", request.url().toString());
                  assertEquals(
                      "Bearer test-token", request.headers().getFirst(HttpHeaders.AUTHORIZATION));

                  return Mono.just(
                      ClientResponse.create(HttpStatus.OK)
                          .header("Content-Type", "application/json")
                          .body("{\"id\":123456789}")
                          .build());
                });

    KakaoOAuthClient client =
        new KakaoOAuthClient(builder, "test-client-id", "test-secret", REDIRECT_URI);

    // When
    KakaoIdentity identity = client.getUserInfo("test-token");

    // Then
    assertEquals("123456789", identity.providerUserId());
  }

  @Test
  @DisplayName("허용 목록의 로컬 Redirect URI로 토큰 교환을 요청한다")
  void exchangesCodeWithLocalRedirectUri() {
    // Given
    String requestedUri = "http://localhost:3000/callback";
    KakaoOAuthClient client = createRedirectUriClient(requestedUri);

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.PROVIDER_UNAVAILABLE, exception.getReason());
  }

  @Test
  @DisplayName("허용 목록의 운영 Redirect URI로 토큰 교환을 요청한다")
  void exchangesCodeWithProductionRedirectUri() {
    // Given
    String requestedUri = "https://app.example.com/callback";
    KakaoOAuthClient client = createRedirectUriClient(requestedUri);

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.PROVIDER_UNAVAILABLE, exception.getReason());
  }

  @Test
  @DisplayName("허용 목록에 없는 Redirect URI는 거부한다")
  void rejectsUnregisteredRedirectUri() {
    // Given
    String requestedUri = "https://untrusted.example.com/callback";
    KakaoOAuthClient client = createRejectingRedirectUriClient();

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  @Test
  @DisplayName("경로가 다른 Redirect URI는 거부한다")
  void rejectsRedirectUriWithDifferentPath() {
    // Given
    String requestedUri = "http://localhost:3000/callback/";
    KakaoOAuthClient client = createRejectingRedirectUriClient();

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  @Test
  @DisplayName("null인 Redirect URI는 거부한다")
  void rejectsNullRedirectUri() {
    // Given
    String requestedUri = null;
    KakaoOAuthClient client = createRejectingRedirectUriClient();

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  @Test
  @DisplayName("빈 Redirect URI는 거부한다")
  void rejectsEmptyRedirectUri() {
    // Given
    String requestedUri = "";
    KakaoOAuthClient client = createRejectingRedirectUriClient();

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  @Test
  @DisplayName("공백만 있는 Redirect URI는 거부한다")
  void rejectsBlankRedirectUri() {
    // Given
    String requestedUri = " ";
    KakaoOAuthClient client = createRejectingRedirectUriClient();

    // When
    KakaoOAuthException exception =
        assertThrows(KakaoOAuthException.class, () -> client.exchangeCode("code", requestedUri));

    // Then
    assertEquals(Reason.INVALID_AUTHORIZATION_CODE, exception.getReason());
  }

  private KakaoOAuthClient createRedirectUriClient(String requestedUri) {
    WebClient.Builder builder =
        WebClient.builder()
            .exchangeFunction(
                request -> {
                  MockClientHttpRequest outgoing =
                      new MockClientHttpRequest(request.method(), request.url());
                  BodyInserter.Context context =
                      new BodyInserter.Context() {
                        @Override
                        public List<HttpMessageWriter<?>> messageWriters() {
                          return ExchangeStrategies.withDefaults().messageWriters();
                        }

                        @Override
                        public Optional<ServerHttpRequest> serverRequest() {
                          return Optional.empty();
                        }

                        @Override
                        public Map<String, Object> hints() {
                          return Map.of();
                        }
                      };
                  return request
                      .body()
                      .insert(outgoing, context)
                      .then(Mono.defer(outgoing::getBodyAsString))
                      .map(
                          body -> {
                            assertTrue(
                                body.contains(
                                    "redirect_uri="
                                        + URLEncoder.encode(requestedUri, StandardCharsets.UTF_8)));
                            return ClientResponse.create(HttpStatus.TOO_MANY_REQUESTS).build();
                          });
                });
    return new KakaoOAuthClient(
        builder,
        "client",
        "secret",
        " http://localhost:3000/callback , , https://app.example.com/callback, ");
  }

  private KakaoOAuthClient createRejectingRedirectUriClient() {
    WebClient.Builder builder =
        WebClient.builder()
            .exchangeFunction(
                request -> {
                  throw new AssertionError("Unregistered URI must not reach provider");
                });
    return new KakaoOAuthClient(
        builder,
        "client",
        "secret",
        "http://localhost:3000/callback,https://app.example.com/callback");
  }
}

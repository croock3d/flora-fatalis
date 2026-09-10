package com.crooked.florafatalis.user.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.crooked.florafatalis.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class AuthControllerIT extends IntegrationTestBase {

  @LocalServerPort private int port;

  private RestClient restClient;

  @BeforeEach
  void setUp() {
    restClient =
        RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
            .build();
  }

  @Test
  void register_success_returns201() {
    // given
    var body =
        """
        {"email":"alice@example.com","password":"secret123","displayName":"Alice"}
        """;

    // when
    HttpStatusCode status = post("/api/auth/register", body);

    // then
    assertThat(status).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void register_duplicateEmail_returns409() {
    // given
    var body =
        """
        {"email":"bob@example.com","password":"secret123","displayName":"Bob"}
        """;
    post("/api/auth/register", body);

    // when
    HttpStatusCode status = post("/api/auth/register", body);

    // then
    assertThat(status).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void login_validCredentials_returnsTokenWithUserInfo() {
    // given
    var registerBody =
        """
        {"email":"charlie@example.com","password":"pass4567","displayName":"Charlie"}
        """;
    post("/api/auth/register", registerBody);
    var loginBody =
        """
        {"email":"charlie@example.com","password":"pass4567"}
        """;

    // when
    ResponseEntity<LoginResponseBody> response =
        restClient
            .post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginBody)
            .retrieve()
            .toEntity(LoginResponseBody.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().token()).isNotBlank();
    assertThat(response.getBody().displayName()).isEqualTo("Charlie");
    assertThat(response.getBody().userId()).isNotBlank();
  }

  @Test
  void login_invalidPassword_returns401() {
    // given
    var registerBody =
        """
        {"email":"dave@example.com","password":"correct1","displayName":"Dave"}
        """;
    post("/api/auth/register", registerBody);
    var loginBody =
        """
        {"email":"dave@example.com","password":"wrong"}
        """;

    // when
    HttpStatusCode status = post("/api/auth/login", loginBody);

    // then
    assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void me_withoutToken_returns401() {
    // when
    HttpStatusCode status =
        restClient.get().uri("/api/me").retrieve().toBodilessEntity().getStatusCode();

    // then
    assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void me_withValidToken_returns200WithUserInfo() {
    // given
    var registerBody =
        """
        {"email":"eve@example.com","password":"eve12345","displayName":"Eve"}
        """;
    post("/api/auth/register", registerBody);
    var loginBody =
        """
        {"email":"eve@example.com","password":"eve12345"}
        """;
    ResponseEntity<LoginResponseBody> loginResponse =
        restClient
            .post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginBody)
            .retrieve()
            .toEntity(LoginResponseBody.class);
    String token = loginResponse.getBody().token();

    // when
    ResponseEntity<MeResponseBody> response =
        restClient
            .get()
            .uri("/api/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .retrieve()
            .toEntity(MeResponseBody.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().displayName()).isEqualTo("Eve");
    assertThat(response.getBody().id()).isNotBlank();
  }

  private HttpStatusCode post(String uri, String jsonBody) {
    return restClient
        .post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(jsonBody)
        .retrieve()
        .toBodilessEntity()
        .getStatusCode();
  }

  record LoginResponseBody(String token, String displayName, String userId) {}

  record MeResponseBody(String id, String displayName) {}
}

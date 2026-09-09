package com.crooked.florafatalis.shared.adapter.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.crooked.florafatalis.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

class HealthControllerIT extends IntegrationTestBase {

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
  void healthReturnsOk() {
    ResponseEntity<String> response =
        restClient.get().uri("/api/health").retrieve().toEntity(String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("ok");
  }
}

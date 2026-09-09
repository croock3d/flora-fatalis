package com.crooked.florafatalis.shared.adapter.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {

  @GetMapping("/api/health")
  HealthResponse health() {
    return new HealthResponse("ok");
  }

  record HealthResponse(String status) {}
}

package com.crooked.florafatalis.shared.adapter.rest;

import com.crooked.florafatalis.shared.adapter.rest.response.MeResponse;
import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
class MeController {

  private final CurrentUserProvider currentUserProvider;

  @GetMapping
  public MeResponse getMe() {
    return new MeResponse(
        currentUserProvider.currentUserId().value().toString(), currentUserProvider.displayName());
  }
}

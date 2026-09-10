package com.crooked.florafatalis.user.adapter.security;

import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

  @Override
  public UserId currentUserId() {
    return new UserId(UUID.fromString(authentication().getPrincipal().toString()));
  }

  @Override
  public String displayName() {
    Object details = authentication().getDetails();
    return details instanceof String name ? name : null;
  }

  private Authentication authentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new IllegalStateException("No authenticated user in context");
    }
    return authentication;
  }
}

package com.crooked.florafatalis.user.adapter.security;

import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import com.crooked.florafatalis.shared.domain.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

  private final SecretKey secretKey;

  public JwtCurrentUserProvider(@Value("${app.jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public UserId currentUserId() {
    return new UserId(UUID.fromString(getClaims().getSubject()));
  }

  @Override
  public String displayName() {
    return getClaims().get("displayName", String.class);
  }

  private Claims getClaims() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getCredentials() == null) {
      throw new IllegalStateException("No authenticated user in context");
    }
    String token = authentication.getCredentials().toString();
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}

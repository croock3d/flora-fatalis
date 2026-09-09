package com.crooked.florafatalis.user.adapter.security;

import com.crooked.florafatalis.user.application.port.out.TokenGenerator;
import com.crooked.florafatalis.user.domain.AuthToken;
import com.crooked.florafatalis.user.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenGenerator implements TokenGenerator {

  private final SecretKey secretKey;
  private final long expirationHours;

  public JwtTokenGenerator(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-hours:24}") long expirationHours) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationHours = expirationHours;
  }

  @Override
  public AuthToken generate(User user) {
    Instant now = Instant.now();
    String token =
        Jwts.builder()
            .subject(user.id().value().toString())
            .claim("displayName", user.displayName().value())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
            .signWith(secretKey)
            .compact();
    return new AuthToken(token);
  }
}

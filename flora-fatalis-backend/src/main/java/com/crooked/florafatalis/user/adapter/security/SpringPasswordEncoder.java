package com.crooked.florafatalis.user.adapter.security;

import com.crooked.florafatalis.user.application.port.out.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringPasswordEncoder implements PasswordEncoder {

  private final org.springframework.security.crypto.password.PasswordEncoder delegate =
      new BCryptPasswordEncoder();

  @Override
  public String encode(String rawPassword) {
    return delegate.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return delegate.matches(rawPassword, encodedPassword);
  }
}

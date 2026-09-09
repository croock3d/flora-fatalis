package com.crooked.florafatalis.user.application.port.out;

public interface PasswordEncoder {

  String encode(String rawPassword);

  boolean matches(String rawPassword, String encodedPassword);
}

package com.crooked.florafatalis.user.domain;

public class EmailAlreadyTakenException extends RuntimeException {

  public EmailAlreadyTakenException(String email) {
    super("Email already taken: " + email);
  }
}

package com.crooked.florafatalis.user.application.port.in;

public interface RegisterUserUseCase {

  void register(RegisterUserCommand command);

  record RegisterUserCommand(String email, String rawPassword, String displayName) {}
}

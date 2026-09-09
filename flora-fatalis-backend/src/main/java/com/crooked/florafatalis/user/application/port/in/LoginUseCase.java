package com.crooked.florafatalis.user.application.port.in;

import java.util.UUID;

public interface LoginUseCase {

  LoginResult login(LoginCommand command);

  record LoginCommand(String email, String rawPassword) {}

  record LoginResult(String token, String displayName, UUID userId) {}
}

package com.crooked.florafatalis.user.adapter.rest;

import com.crooked.florafatalis.user.application.port.in.LoginUseCase;
import com.crooked.florafatalis.user.application.port.in.LoginUseCase.LoginCommand;
import com.crooked.florafatalis.user.application.port.in.LoginUseCase.LoginResult;
import com.crooked.florafatalis.user.application.port.in.RegisterUserUseCase;
import com.crooked.florafatalis.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  void register(@RequestBody RegisterRequest request) {
    registerUserUseCase.register(
        new RegisterUserCommand(request.email(), request.password(), request.displayName()));
  }

  @PostMapping("/login")
  LoginResponse login(@RequestBody LoginRequest request) {
    LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
    return new LoginResponse(result.token(), result.displayName(), result.userId());
  }

  record RegisterRequest(String email, String password, String displayName) {}

  record LoginRequest(String email, String password) {}

  record LoginResponse(String token, String displayName, UUID userId) {}
}

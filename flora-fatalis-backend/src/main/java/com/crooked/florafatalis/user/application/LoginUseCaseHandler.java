package com.crooked.florafatalis.user.application;

import com.crooked.florafatalis.user.application.port.in.LoginUseCase;
import com.crooked.florafatalis.user.application.port.out.PasswordEncoder;
import com.crooked.florafatalis.user.application.port.out.TokenGenerator;
import com.crooked.florafatalis.user.application.port.out.UserRepository;
import com.crooked.florafatalis.user.domain.AuthToken;
import com.crooked.florafatalis.user.domain.Email;
import com.crooked.florafatalis.user.domain.InvalidCredentialsException;
import com.crooked.florafatalis.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCaseHandler implements LoginUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;

  @Override
  public LoginResult login(LoginCommand command) {
    Email email = new Email(command.email());
    User user =
        userRepository.findByEmail(email.value()).orElseThrow(InvalidCredentialsException::new);
    if (!passwordEncoder.matches(command.rawPassword(), user.hashedPassword().value())) {
      throw new InvalidCredentialsException();
    }
    AuthToken token = tokenGenerator.generate(user);
    return new LoginResult(token.value(), user.displayName().value(), user.id().value());
  }
}

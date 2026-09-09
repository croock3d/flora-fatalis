package com.crooked.florafatalis.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.user.application.port.in.LoginUseCase;
import com.crooked.florafatalis.user.application.port.in.LoginUseCase.LoginCommand;
import com.crooked.florafatalis.user.application.port.in.LoginUseCase.LoginResult;
import com.crooked.florafatalis.user.application.port.out.PasswordEncoder;
import com.crooked.florafatalis.user.application.port.out.TokenGenerator;
import com.crooked.florafatalis.user.application.port.out.UserRepository;
import com.crooked.florafatalis.user.domain.AuthToken;
import com.crooked.florafatalis.user.domain.DisplayName;
import com.crooked.florafatalis.user.domain.Email;
import com.crooked.florafatalis.user.domain.HashedPassword;
import com.crooked.florafatalis.user.domain.InvalidCredentialsException;
import com.crooked.florafatalis.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseHandlerTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private TokenGenerator tokenGenerator;

  private LoginUseCase useCase;

  private final User existingUser =
      User.register(
          UserId.newId(),
          new Email("jan@example.com"),
          new HashedPassword("$2a$hashed"),
          new DisplayName("Jan Kowalski"));

  @BeforeEach
  void setUp() {
    useCase = new LoginUseCaseHandler(userRepository, passwordEncoder, tokenGenerator);
  }

  @Test
  void successfulLoginReturnsToken() {
    // given
    LoginCommand command = new LoginCommand("jan@example.com", "correctPassword");
    given(userRepository.findByEmail("jan@example.com")).willReturn(Optional.of(existingUser));
    given(passwordEncoder.matches("correctPassword", "$2a$hashed")).willReturn(true);
    given(tokenGenerator.generate(existingUser)).willReturn(new AuthToken("jwt.token.value"));

    // when
    LoginResult result = useCase.login(command);

    // then
    assertThat(result.token()).isEqualTo("jwt.token.value");
    assertThat(result.displayName()).isEqualTo("Jan Kowalski");
    assertThat(result.userId()).isEqualTo(existingUser.id().value());
    then(tokenGenerator).should().generate(existingUser);
  }

  @Test
  void unknownEmailThrowsInvalidCredentialsException() {
    // given
    LoginCommand command = new LoginCommand("unknown@example.com", "anyPassword");
    given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> useCase.login(command))
        .isInstanceOf(InvalidCredentialsException.class);
    then(passwordEncoder).shouldHaveNoInteractions();
    then(tokenGenerator).shouldHaveNoInteractions();
  }

  @Test
  void wrongPasswordThrowsInvalidCredentialsException() {
    // given
    LoginCommand command = new LoginCommand("jan@example.com", "wrongPassword");
    given(userRepository.findByEmail("jan@example.com")).willReturn(Optional.of(existingUser));
    given(passwordEncoder.matches("wrongPassword", "$2a$hashed")).willReturn(false);

    // when / then
    assertThatThrownBy(() -> useCase.login(command))
        .isInstanceOf(InvalidCredentialsException.class);
    then(tokenGenerator).shouldHaveNoInteractions();
  }
}

package com.crooked.florafatalis.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.user.application.port.in.RegisterUserUseCase;
import com.crooked.florafatalis.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.crooked.florafatalis.user.application.port.out.CreateOwnerHouseholdPort;
import com.crooked.florafatalis.user.application.port.out.PasswordEncoder;
import com.crooked.florafatalis.user.application.port.out.UserRepository;
import com.crooked.florafatalis.user.domain.EmailAlreadyTakenException;
import com.crooked.florafatalis.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseHandlerTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private CreateOwnerHouseholdPort createOwnerHouseholdPort;

  private RegisterUserUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new RegisterUserUseCaseHandler(userRepository, passwordEncoder, createOwnerHouseholdPort);
  }

  @Test
  void successfulRegistrationSavesUserAndCreatesHousehold() {
    RegisterUserCommand command =
        new RegisterUserCommand("jan@example.com", "plainPassword1", "Jan Kowalski");
    given(userRepository.existsByEmail("jan@example.com")).willReturn(false);
    given(passwordEncoder.encode("plainPassword1")).willReturn("$2a$hashed");

    useCase.register(command);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    then(userRepository).should().save(captor.capture());
    User saved = captor.getValue();
    assertThat(saved.email().value()).isEqualTo("jan@example.com");
    assertThat(saved.displayName().value()).isEqualTo("Jan Kowalski");
    assertThat(saved.hashedPassword().value()).isEqualTo("$2a$hashed");
    assertThat(saved.id()).isNotNull();
    assertThat(saved.createdAt()).isNotNull();
    then(createOwnerHouseholdPort).should().createFor(saved.id());
  }

  @Test
  void plainPasswordIsNeverStoredInUser() {
    RegisterUserCommand command =
        new RegisterUserCommand("jan@example.com", "plainPassword1", "Jan Kowalski");
    given(userRepository.existsByEmail("jan@example.com")).willReturn(false);
    given(passwordEncoder.encode("plainPassword1")).willReturn("$2a$hashed");

    useCase.register(command);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    then(userRepository).should().save(captor.capture());
    assertThat(captor.getValue().hashedPassword().value()).doesNotContain("plainPassword1");
  }

  @Test
  void duplicateEmailThrowsEmailAlreadyTakenException() {
    RegisterUserCommand command =
        new RegisterUserCommand("jan@example.com", "plainPassword1", "Jan Kowalski");
    given(userRepository.existsByEmail("jan@example.com")).willReturn(true);

    assertThatThrownBy(() -> useCase.register(command))
        .isInstanceOf(EmailAlreadyTakenException.class)
        .hasMessageContaining("jan@example.com");
    then(userRepository).should().existsByEmail("jan@example.com");
    then(userRepository).shouldHaveNoMoreInteractions();
    then(passwordEncoder).shouldHaveNoInteractions();
    then(createOwnerHouseholdPort).shouldHaveNoInteractions();
  }
}

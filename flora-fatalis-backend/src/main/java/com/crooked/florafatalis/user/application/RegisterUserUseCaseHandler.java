package com.crooked.florafatalis.user.application;

import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.user.application.port.in.RegisterUserUseCase;
import com.crooked.florafatalis.user.application.port.out.CreateOwnerHouseholdPort;
import com.crooked.florafatalis.user.application.port.out.PasswordEncoder;
import com.crooked.florafatalis.user.application.port.out.UserRepository;
import com.crooked.florafatalis.user.domain.DisplayName;
import com.crooked.florafatalis.user.domain.Email;
import com.crooked.florafatalis.user.domain.EmailAlreadyTakenException;
import com.crooked.florafatalis.user.domain.HashedPassword;
import com.crooked.florafatalis.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCaseHandler implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CreateOwnerHouseholdPort createOwnerHouseholdPort;

  @Override
  @Transactional
  public void register(RegisterUserCommand command) {
    Email email = new Email(command.email());
    if (userRepository.existsByEmail(email.value())) {
      throw new EmailAlreadyTakenException(email.value());
    }
    HashedPassword hashedPassword =
        new HashedPassword(passwordEncoder.encode(command.rawPassword()));
    DisplayName displayName = new DisplayName(command.displayName());
    User user = User.register(UserId.newId(), email, hashedPassword, displayName);
    userRepository.save(user);
    createOwnerHouseholdPort.createFor(user.id());
  }
}

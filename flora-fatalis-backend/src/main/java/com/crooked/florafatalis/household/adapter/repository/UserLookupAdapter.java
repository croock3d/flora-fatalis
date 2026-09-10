package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.user.adapter.repository.jpa.UserJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLookupAdapter implements UserLookupPort {

  private final UserJpaRepository userJpaRepository;

  @Override
  public Optional<UserId> findUserIdByEmail(String email) {
    String normalized = email == null ? "" : email.toLowerCase().strip();
    return userJpaRepository.findByEmail(normalized).map(entity -> new UserId(entity.getId()));
  }

  @Override
  public String findDisplayNameById(UserId userId) {
    return userJpaRepository
        .findById(userId.value())
        .map(entity -> entity.getDisplayName())
        .orElse("Unknown");
  }
}

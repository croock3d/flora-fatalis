package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.user.adapter.repository.jpa.UserJpaRepository;
import com.crooked.florafatalis.user.adapter.repository.jpa.entity.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActiveHouseholdAdapter implements UserActiveHouseholdPort {

  private final UserJpaRepository userJpaRepository;

  @Override
  public Optional<HouseholdId> get(UserId userId) {
    return userJpaRepository
        .findById(userId.value())
        .map(UserEntity::getActiveHouseholdId)
        .filter(java.util.Objects::nonNull)
        .map(HouseholdId::new);
  }

  @Override
  public void set(UserId userId, HouseholdId householdId) {
    UserEntity entity =
        userJpaRepository
            .findById(userId.value())
            .orElseThrow(() -> new IllegalStateException("User not found"));
    entity.setActiveHouseholdId(householdId.value());
    userJpaRepository.save(entity);
  }
}

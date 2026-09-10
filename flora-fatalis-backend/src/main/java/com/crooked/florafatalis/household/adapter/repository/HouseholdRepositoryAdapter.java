package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.adapter.repository.jpa.HouseholdJpaRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HouseholdRepositoryAdapter implements HouseholdRepository {

  private final HouseholdJpaRepository jpaRepository;

  @Override
  public void save(Household household) {
    jpaRepository.save(HouseholdEntityMapper.toEntity(household));
  }

  @Override
  public Optional<Household> findById(HouseholdId id) {
    return jpaRepository.findById(id.value()).map(HouseholdEntityMapper::toDomain);
  }

  @Override
  public Optional<Household> findOwnedBy(UserId userId) {
    return jpaRepository.findByOwnerId(userId.value()).map(HouseholdEntityMapper::toDomain);
  }

  @Override
  public List<Household> findAllByMember(UserId userId) {
    return jpaRepository.findAllByMember(userId.value()).stream()
        .map(HouseholdEntityMapper::toDomain)
        .toList();
  }
}

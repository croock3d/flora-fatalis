package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.adapter.repository.jpa.HouseholdInvitationJpaRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HouseholdInvitationRepositoryAdapter implements HouseholdInvitationRepository {

  private final HouseholdInvitationJpaRepository jpaRepository;

  @Override
  public void save(HouseholdInvitation invitation) {
    jpaRepository.save(HouseholdInvitationEntityMapper.toEntity(invitation));
  }

  @Override
  public Optional<HouseholdInvitation> findById(HouseholdInvitationId id) {
    return jpaRepository.findById(id.value()).map(HouseholdInvitationEntityMapper::toDomain);
  }

  @Override
  public Optional<HouseholdInvitation> findByHouseholdId(HouseholdId householdId) {
    return jpaRepository
        .findByHouseholdId(householdId.value())
        .map(HouseholdInvitationEntityMapper::toDomain);
  }

  @Override
  public Optional<HouseholdInvitation> findByInvitee(UserId inviteeId) {
    return jpaRepository
        .findByInviteeId(inviteeId.value())
        .map(HouseholdInvitationEntityMapper::toDomain);
  }

  @Override
  public void delete(HouseholdInvitationId id) {
    jpaRepository.deleteById(id.value());
  }
}

package com.crooked.florafatalis.household.adapter.repository.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdInvitationJpaRepository
    extends JpaRepository<HouseholdInvitationEntity, UUID> {

  Optional<HouseholdInvitationEntity> findByHouseholdId(UUID householdId);

  Optional<HouseholdInvitationEntity> findByInviterId(UUID inviterId);

  Optional<HouseholdInvitationEntity> findByInviteeId(UUID inviteeId);
}

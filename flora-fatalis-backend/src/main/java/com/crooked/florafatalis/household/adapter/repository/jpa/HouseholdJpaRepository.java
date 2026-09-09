package com.crooked.florafatalis.household.adapter.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseholdJpaRepository extends JpaRepository<HouseholdEntity, UUID> {

  Optional<HouseholdEntity> findByOwnerId(UUID ownerId);

  @Query("SELECT h FROM HouseholdEntity h WHERE h.ownerId = :userId OR h.partnerId = :userId")
  List<HouseholdEntity> findAllByMember(@Param("userId") UUID userId);
}

package com.crooked.florafatalis.location.adapter.repository.jpa;

import com.crooked.florafatalis.location.adapter.repository.jpa.entity.LocationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationJpaRepository extends JpaRepository<LocationEntity, UUID> {

  List<LocationEntity> findByHouseholdId(UUID householdId);
}

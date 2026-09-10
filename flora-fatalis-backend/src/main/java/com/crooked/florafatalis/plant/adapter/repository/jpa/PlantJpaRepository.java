package com.crooked.florafatalis.plant.adapter.repository.jpa;

import com.crooked.florafatalis.plant.adapter.repository.jpa.entity.PlantEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantJpaRepository extends JpaRepository<PlantEntity, UUID> {

  List<PlantEntity> findByHouseholdIdAndArchivedAtIsNull(UUID householdId);

  boolean existsByLocationIdAndArchivedAtIsNull(UUID locationId);
}

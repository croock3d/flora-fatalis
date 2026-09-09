package com.crooked.florafatalis.care.adapter.repository.jpa;

import com.crooked.florafatalis.care.adapter.repository.jpa.entity.CareEventEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareEventJpaRepository extends JpaRepository<CareEventEntity, UUID> {

  List<CareEventEntity> findByPlantIdAndCareTypeOrderByPerformedAtDesc(
      UUID plantId, String careType);

  Optional<CareEventEntity> findFirstByPlantIdAndCareTypeOrderByPerformedAtDesc(
      UUID plantId, String careType);

  List<CareEventEntity> findByHouseholdIdAndCareTypeOrderByPerformedAtDesc(
      UUID householdId, String careType);

  List<CareEventEntity> findByHouseholdId(UUID householdId);
}

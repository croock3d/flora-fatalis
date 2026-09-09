package com.crooked.florafatalis.photo.adapter.repository.jpa;

import com.crooked.florafatalis.photo.adapter.repository.jpa.entity.PlantPhotoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantPhotoJpaRepository extends JpaRepository<PlantPhotoEntity, UUID> {

  List<PlantPhotoEntity> findByPlantIdOrderByPrimaryDescTakenAtDesc(UUID plantId);

  Optional<PlantPhotoEntity> findFirstByPlantIdAndPrimaryIsTrue(UUID plantId);

  List<PlantPhotoEntity> findByHouseholdId(UUID householdId);
}

package com.crooked.florafatalis.photo.adapter.repository.jpa;

import com.crooked.florafatalis.photo.adapter.repository.jpa.entity.PlantPhotoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlantPhotoJpaRepository extends JpaRepository<PlantPhotoEntity, UUID> {

  List<PlantPhotoEntity> findByPlantIdOrderByPrimaryDescTakenAtDesc(UUID plantId);

  @Query(
      value =
          "SELECT * FROM plant_photos WHERE plant_id = :plantId AND is_primary = true ORDER BY taken_at DESC LIMIT 1",
      nativeQuery = true)
  Optional<PlantPhotoEntity> findFirstByPlantIdAndPrimaryIsTrue(@Param("plantId") UUID plantId);

  @Query(
      value = "SELECT * FROM plant_photos WHERE household_id = :householdId AND is_primary = true",
      nativeQuery = true)
  List<PlantPhotoEntity> findByHouseholdIdAndPrimaryIsTrue(@Param("householdId") UUID householdId);
}

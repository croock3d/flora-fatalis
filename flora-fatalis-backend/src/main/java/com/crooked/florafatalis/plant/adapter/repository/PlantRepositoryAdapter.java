package com.crooked.florafatalis.plant.adapter.repository;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.adapter.repository.jpa.PlantJpaRepository;
import com.crooked.florafatalis.plant.adapter.repository.jpa.entity.PlantEntity;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlantRepositoryAdapter implements PlantRepository {

  private final PlantJpaRepository jpaRepository;

  @Override
  public void save(Plant plant) {
    jpaRepository.save(toEntity(plant));
  }

  @Override
  public Optional<Plant> findById(PlantId id) {
    return jpaRepository.findById(id.value()).map(this::toDomain);
  }

  @Override
  public List<Plant> findActiveByHousehold(HouseholdId householdId) {
    return jpaRepository.findByHouseholdIdAndArchivedAtIsNull(householdId.value()).stream()
        .map(this::toDomain)
        .toList();
  }

  private PlantEntity toEntity(Plant plant) {
    PlantEntity entity = new PlantEntity();
    entity.setId(plant.id().value());
    entity.setHouseholdId(plant.householdId().value());
    entity.setSpeciesId(plant.speciesId().value());
    entity.setLocationId(plant.locationId().value());
    entity.setName(plant.name());
    entity.setWateringIntervalDaysOverride(plant.wateringIntervalDaysOverride());
    entity.setFertilizingIntervalDaysOverride(plant.fertilizingIntervalDaysOverride());
    entity.setAcquiredAt(plant.acquiredAt());
    entity.setArchivedAt(plant.archivedAt());
    entity.setCreatedBy(plant.createdBy().value());
    entity.setCreatedAt(plant.createdAt());
    return entity;
  }

  private Plant toDomain(PlantEntity entity) {
    return new Plant(
        new PlantId(entity.getId()),
        new HouseholdId(entity.getHouseholdId()),
        new SpeciesId(entity.getSpeciesId()),
        new LocationId(entity.getLocationId()),
        entity.getName(),
        entity.getWateringIntervalDaysOverride(),
        entity.getFertilizingIntervalDaysOverride(),
        entity.getAcquiredAt(),
        entity.getArchivedAt(),
        new UserId(entity.getCreatedBy()),
        entity.getCreatedAt());
  }
}

package com.crooked.florafatalis.photo.adapter.repository;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.photo.adapter.repository.jpa.PlantPhotoJpaRepository;
import com.crooked.florafatalis.photo.adapter.repository.jpa.entity.PlantPhotoEntity;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.plant.domain.PlantId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlantPhotoRepositoryAdapter implements PlantPhotoRepository {

  private final PlantPhotoJpaRepository jpaRepository;

  @Override
  public void save(PlantPhoto photo) {
    jpaRepository.save(toEntity(photo));
  }

  @Override
  public Optional<PlantPhoto> findById(PlantPhotoId id) {
    return jpaRepository.findById(id.value()).map(this::toDomain);
  }

  @Override
  public List<PlantPhoto> findByPlantId(PlantId plantId) {
    return jpaRepository.findByPlantIdOrderByPrimaryDescTakenAtDesc(plantId.value()).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<PlantPhoto> findPrimaryByPlantId(PlantId plantId) {
    return jpaRepository.findFirstByPlantIdAndPrimaryIsTrue(plantId.value()).map(this::toDomain);
  }

  @Override
  public List<PlantPhoto> findPrimaryByHouseholdId(HouseholdId householdId) {
    return jpaRepository.findByHouseholdIdAndPrimaryIsTrue(householdId.value()).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public void delete(PlantPhotoId id) {
    jpaRepository.deleteById(id.value());
  }

  private PlantPhotoEntity toEntity(PlantPhoto photo) {
    PlantPhotoEntity entity = new PlantPhotoEntity();
    entity.setId(photo.id().value());
    entity.setPlantId(photo.plantId().value());
    entity.setHouseholdId(photo.householdId().value());
    entity.setStorageKey(photo.storageKey());
    entity.setContentType(photo.contentType());
    entity.setTakenAt(photo.takenAt());
    entity.setPrimary(photo.primary());
    entity.setSortOrder(photo.sortOrder());
    return entity;
  }

  private PlantPhoto toDomain(PlantPhotoEntity entity) {
    return new PlantPhoto(
        new PlantPhotoId(entity.getId()),
        new PlantId(entity.getPlantId()),
        new HouseholdId(entity.getHouseholdId()),
        entity.getStorageKey(),
        entity.getContentType(),
        entity.getTakenAt(),
        entity.isPrimary(),
        entity.getSortOrder());
  }
}

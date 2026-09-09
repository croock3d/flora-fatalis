package com.crooked.florafatalis.care.adapter.repository;

import com.crooked.florafatalis.care.adapter.repository.jpa.CareEventJpaRepository;
import com.crooked.florafatalis.care.adapter.repository.jpa.entity.CareEventEntity;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareEventId;
import com.crooked.florafatalis.care.domain.CareSource;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.PruningKind;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CareEventRepositoryAdapter implements CareEventRepository {

  private final CareEventJpaRepository jpaRepository;

  @Override
  public void save(CareEvent event) {
    jpaRepository.save(toEntity(event));
  }

  @Override
  public void delete(CareEventId id) {
    jpaRepository.deleteById(id.value());
  }

  @Override
  public Optional<CareEvent> findById(CareEventId id) {
    return jpaRepository.findById(id.value()).map(this::toDomain);
  }

  @Override
  public List<CareEvent> findByPlantIdAndCareType(PlantId plantId, CareType careType) {
    return jpaRepository
        .findByPlantIdAndCareTypeOrderByPerformedAtDesc(plantId.value(), careType.name())
        .stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<CareEvent> findLatest(PlantId plantId, CareType careType) {
    return jpaRepository
        .findFirstByPlantIdAndCareTypeOrderByPerformedAtDesc(plantId.value(), careType.name())
        .map(this::toDomain);
  }

  @Override
  public List<CareEvent> findLatestByHouseholdAndCareType(
      HouseholdId householdId, CareType careType) {
    LinkedHashMap<PlantId, CareEvent> latest = new LinkedHashMap<>();
    jpaRepository
        .findByHouseholdIdAndCareTypeOrderByPerformedAtDesc(householdId.value(), careType.name())
        .stream()
        .map(this::toDomain)
        .forEach(event -> latest.putIfAbsent(event.plantId(), event));
    return List.copyOf(latest.values());
  }

  private CareEventEntity toEntity(CareEvent event) {
    CareEventEntity entity = new CareEventEntity();
    entity.setId(event.id().value());
    entity.setPlantId(event.plantId().value());
    entity.setHouseholdId(event.householdId().value());
    entity.setCareType(event.careType().name());
    entity.setPerformedAt(event.performedAt());
    entity.setPerformedBy(event.performedBy().value());
    entity.setQuantityMl(event.quantityMl());
    entity.setSource(event.source().name());
    entity.setNotes(event.notes());
    entity.setPruningKind(event.pruningKind() == null ? null : event.pruningKind().name());
    return entity;
  }

  private CareEvent toDomain(CareEventEntity entity) {
    return new CareEvent(
        new CareEventId(entity.getId()),
        new PlantId(entity.getPlantId()),
        new HouseholdId(entity.getHouseholdId()),
        CareType.valueOf(entity.getCareType()),
        entity.getPerformedAt(),
        new UserId(entity.getPerformedBy()),
        entity.getQuantityMl(),
        CareSource.valueOf(entity.getSource()),
        entity.getNotes(),
        entity.getPruningKind() == null ? null : PruningKind.valueOf(entity.getPruningKind()));
  }
}

package com.crooked.florafatalis.location.adapter.repository;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.adapter.repository.jpa.LocationJpaRepository;
import com.crooked.florafatalis.location.adapter.repository.jpa.entity.LocationEntity;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.location.domain.LocationKind;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryAdapter implements LocationRepository {

  private final LocationJpaRepository jpaRepository;

  @Override
  public void save(Location location) {
    jpaRepository.save(toEntity(location));
  }

  @Override
  public Optional<Location> findById(LocationId id) {
    return jpaRepository.findById(id.value()).map(this::toDomain);
  }

  @Override
  public List<Location> findByHousehold(HouseholdId householdId) {
    return jpaRepository.findByHouseholdId(householdId.value()).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public void delete(LocationId id) {
    jpaRepository.deleteById(id.value());
  }

  private LocationEntity toEntity(Location location) {
    LocationEntity entity = new LocationEntity();
    entity.setId(location.id().value());
    entity.setHouseholdId(location.householdId().value());
    entity.setName(location.name());
    entity.setKind(location.kind().name());
    return entity;
  }

  private Location toDomain(LocationEntity entity) {
    return new Location(
        new LocationId(entity.getId()),
        new HouseholdId(entity.getHouseholdId()),
        entity.getName(),
        LocationKind.valueOf(entity.getKind()));
  }
}

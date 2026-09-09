package com.crooked.florafatalis.species.adapter.repository;

import com.crooked.florafatalis.species.adapter.repository.jpa.SpeciesJpaRepository;
import com.crooked.florafatalis.species.adapter.repository.jpa.entity.SpeciesEntity;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SpeciesRepositoryAdapter implements SpeciesRepository {

  private final SpeciesJpaRepository jpaRepository;

  @Override
  public List<Species> findAll() {
    return jpaRepository.findAll().stream()
        .map(this::toDomain)
        .sorted(
            Comparator.comparing((Species species) -> "Inny".equals(species.name()))
                .thenComparing(species -> species.category() == null ? "" : species.category())
                .thenComparing(Species::name, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  @Override
  public Optional<Species> findById(SpeciesId id) {
    return jpaRepository.findById(id.value()).map(this::toDomain);
  }

  private Species toDomain(SpeciesEntity entity) {
    return new Species(
        new SpeciesId(entity.getId()),
        entity.getName(),
        entity.getLatinName(),
        entity.getDefaultWateringIntervalDays(),
        entity.getWateringIntervalMinDays(),
        entity.getWateringIntervalMaxDays(),
        entity.getWateringIntervalLabel(),
        entity.getLightPreference(),
        entity.getHumidityPreference(),
        entity.getCategory(),
        entity.getDefaultFertilizingIntervalDays(),
        entity.getFertilizingIntervalMinDays(),
        entity.getFertilizingIntervalMaxDays(),
        entity.getFertilizingIntervalLabel(),
        entity.getFertilizingSeason(),
        entity.getFertilizerType(),
        entity.getFertilizerForm(),
        entity.getFertilizingNotes(),
        entity.getFertilizingRestPeriod());
  }
}

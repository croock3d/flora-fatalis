package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.GetPlantCareStatusUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CarePolicy;
import com.crooked.florafatalis.care.domain.CareRecommendation;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.FertilizingCarePolicy;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPlantCareStatusUseCaseHandler implements GetPlantCareStatusUseCase {

  private final PlantRepository plantRepository;
  private final SpeciesRepository speciesRepository;
  private final CareEventRepository careEventRepository;
  private final CarePolicy carePolicy;
  private final FertilizingCarePolicy fertilizingCarePolicy;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final Clock clock;

  @Value("${app.timezone:Europe/Warsaw}")
  private String timezone;

  @Override
  public PlantCareStatus get(UserId userId, PlantId plantId) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(userId)
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant = plantRepository.findById(plantId).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    Species species =
        speciesRepository.findById(plant.speciesId()).orElseThrow(SpeciesNotFoundException::new);
    ZoneId zone = ZoneId.of(timezone);
    LocalDate today = LocalDate.now(clock.withZone(zone));
    Optional<Instant> lastWatering =
        careEventRepository.findLatest(plantId, CareType.WATERING).map(CareEvent::performedAt);
    Optional<Instant> lastFertilizing =
        careEventRepository.findLatest(plantId, CareType.FERTILIZING).map(CareEvent::performedAt);
    CareRecommendation watering = carePolicy.recommend(plant, species, lastWatering, today, zone);
    CareTypeStatus wateringStatus =
        new CareTypeStatus(lastWatering.orElse(null), watering.dueOn(), watering.intervalDays());
    CareTypeStatus fertilizingStatus =
        fertilizingCarePolicy
            .recommend(plant, species, lastFertilizing, today, zone)
            .map(
                recommendation ->
                    new CareTypeStatus(
                        lastFertilizing.orElse(null),
                        recommendation.dueOn(),
                        recommendation.intervalDays()))
            .orElse(null);
    return new PlantCareStatus(wateringStatus, fertilizingStatus);
  }
}

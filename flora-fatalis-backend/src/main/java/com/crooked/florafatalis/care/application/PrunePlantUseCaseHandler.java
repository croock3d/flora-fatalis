package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.PrunePlantUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrunePlantUseCaseHandler implements PrunePlantUseCase {

  private final PlantRepository plantRepository;
  private final CareEventRepository careEventRepository;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final Clock clock;

  @Value("${app.timezone:Europe/Warsaw}")
  private String timezone;

  @Override
  public CareEvent prune(PrunePlantCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant =
        plantRepository.findById(command.plantId()).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId) || plant.isArchived()) {
      throw new HouseholdAccessDeniedException();
    }
    CareEvent event =
        CareEvent.pruning(
            plant.id(),
            householdId,
            command.userId(),
            resolvePerformedAt(command.performedOn()),
            command.pruningKind(),
            command.notes());
    careEventRepository.save(event);
    return event;
  }

  private Instant resolvePerformedAt(LocalDate performedOn) {
    ZoneId zone = ZoneId.of(timezone);
    LocalDate today = LocalDate.now(clock.withZone(zone));
    if (performedOn == null || performedOn.equals(today)) {
      return Instant.now(clock);
    }
    if (performedOn.isAfter(today)) {
      throw new IllegalArgumentException("performedOn must not be in the future");
    }
    return performedOn.atTime(LocalTime.NOON).atZone(zone).toInstant();
  }
}

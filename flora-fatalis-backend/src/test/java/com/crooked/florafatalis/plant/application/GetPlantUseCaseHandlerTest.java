package com.crooked.florafatalis.plant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.in.GetPlantUseCase.GetPlantCommand;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private GetPlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler = new GetPlantUseCaseHandler(plantRepository, activeHouseholdPort);
  }

  @Test
  void returnsPlantInOwnHousehold() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThat(handler.get(new GetPlantCommand(userId, plant.id()))).isEqualTo(plant);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant = plant(HouseholdId.newId());
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(() -> handler.get(new GetPlantCommand(userId, plant.id())))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  @Test
  void missingPlantThrows() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.empty());

    assertThatThrownBy(() -> handler.get(new GetPlantCommand(userId, plant.id())))
        .isInstanceOf(PlantNotFoundException.class);
  }

  private Plant plant(HouseholdId household) {
    return Plant.create(
        household,
        SpeciesId.newId(),
        LocationId.newId(),
        "Monstera",
        null,
        null,
        null,
        userId,
        now);
  }
}

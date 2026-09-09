package com.crooked.florafatalis.plant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListPlantsUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private ListPlantsUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler = new ListPlantsUseCaseHandler(plantRepository, activeHouseholdPort);
  }

  @Test
  void listsActivePlantsForHousehold() {
    Plant plant =
        Plant.create(
            householdId,
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            userId,
            Instant.parse("2026-01-01T00:00:00Z"));
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of(plant));

    assertThat(handler.list(userId)).containsExactly(plant);
  }

  @Test
  void noActiveHouseholdReturnsEmptyList() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.empty());

    assertThat(handler.list(userId)).isEmpty();
  }
}

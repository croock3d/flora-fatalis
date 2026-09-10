package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.care.application.port.in.GetPlantCareStatusUseCase.PlantCareStatus;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.FertilizingCarePolicy;
import com.crooked.florafatalis.care.domain.IntervalCarePolicy;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPlantCareStatusUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private SpeciesRepository speciesRepository;
  @Mock private CareEventRepository careEventRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private GetPlantCareStatusUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-10T08:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final Species species =
      new Species(
          speciesId,
          "Monstera",
          null,
          7,
          null,
          null,
          null,
          null,
          null,
          null,
          30,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);

  @BeforeEach
  void setUp() {
    handler =
        new GetPlantCareStatusUseCaseHandler(
            plantRepository,
            speciesRepository,
            careEventRepository,
            new IntervalCarePolicy(),
            new FertilizingCarePolicy(),
            activeHouseholdPort,
            Clock.fixed(now, ZoneOffset.UTC),
            ZoneOffset.UTC);
  }

  @Test
  void returnsWateringAndFertilizingStatus() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));
    given(speciesRepository.findById(speciesId)).willReturn(Optional.of(species));
    given(careEventRepository.findLatest(plant.id(), CareType.WATERING))
        .willReturn(Optional.empty());
    given(careEventRepository.findLatest(plant.id(), CareType.FERTILIZING))
        .willReturn(Optional.empty());

    PlantCareStatus status = handler.get(userId, plant.id());

    assertThat(status.watering().nextOn()).isEqualTo(LocalDate.parse("2026-01-17"));
    assertThat(status.watering().intervalDays()).isEqualTo(7);
    assertThat(status.fertilizing().nextOn()).isEqualTo(LocalDate.parse("2026-02-09"));
    assertThat(status.fertilizing().intervalDays()).isEqualTo(30);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant = plant(HouseholdId.newId());
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(() -> handler.get(userId, plant.id()))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  private Plant plant(HouseholdId household) {
    return Plant.create(
        household, speciesId, LocationId.newId(), "Monstera", null, null, null, userId, now);
  }
}

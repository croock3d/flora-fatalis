package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.care.application.port.in.FertilizePlantUseCase.FertilizePlantCommand;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FertilizePlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private CareEventRepository careEventRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private FertilizePlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-10T08:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new FertilizePlantUseCaseHandler(
            plantRepository,
            careEventRepository,
            activeHouseholdPort,
            Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void recordsFertilizingEvent() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    CareEvent event = handler.fertilize(new FertilizePlantCommand(userId, plant.id()));

    ArgumentCaptor<CareEvent> captor = ArgumentCaptor.forClass(CareEvent.class);
    then(careEventRepository).should().save(captor.capture());
    assertThat(captor.getValue().careType()).isEqualTo(CareType.FERTILIZING);
    assertThat(captor.getValue().performedAt()).isEqualTo(now);
    assertThat(captor.getValue().performedBy()).isEqualTo(userId);
    assertThat(captor.getValue().quantityMl()).isNull();
    assertThat(event).isEqualTo(captor.getValue());
  }

  @Test
  void archivedPlantThrows() {
    Plant plant = plant(householdId).archive(now);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(() -> handler.fertilize(new FertilizePlantCommand(userId, plant.id())))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant = plant(HouseholdId.newId());
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(() -> handler.fertilize(new FertilizePlantCommand(userId, plant.id())))
        .isInstanceOf(HouseholdAccessDeniedException.class);
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

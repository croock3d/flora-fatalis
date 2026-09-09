package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.care.application.port.in.WaterPlantUseCase.WaterPlantCommand;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
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
class WaterPlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private CareEventRepository careEventRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private WaterPlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-10T08:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new WaterPlantUseCaseHandler(
            plantRepository,
            careEventRepository,
            activeHouseholdPort,
            Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void recordsManualWatering() {
    Plant plant =
        Plant.create(
            householdId,
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            userId,
            now);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    CareEvent event = handler.water(new WaterPlantCommand(userId, plant.id(), null));

    ArgumentCaptor<CareEvent> captor = ArgumentCaptor.forClass(CareEvent.class);
    then(careEventRepository).should().save(captor.capture());
    assertThat(captor.getValue().careType()).isEqualTo(CareType.WATERING);
    assertThat(captor.getValue().performedAt()).isEqualTo(now);
    assertThat(captor.getValue().performedBy()).isEqualTo(userId);
    assertThat(captor.getValue().quantityMl()).isNull();
    assertThat(event).isEqualTo(captor.getValue());
  }

  @Test
  void recordsOptionalQuantity() {
    Plant plant =
        Plant.create(
            householdId,
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            userId,
            now);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    handler.water(new WaterPlantCommand(userId, plant.id(), 250));

    ArgumentCaptor<CareEvent> captor = ArgumentCaptor.forClass(CareEvent.class);
    then(careEventRepository).should().save(captor.capture());
    assertThat(captor.getValue().quantityMl()).isEqualTo(250);
  }
}

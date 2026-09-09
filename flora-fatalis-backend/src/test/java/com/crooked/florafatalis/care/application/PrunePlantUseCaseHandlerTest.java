package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.care.application.port.in.PrunePlantUseCase.PrunePlantCommand;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.PruningKind;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PrunePlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private CareEventRepository careEventRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private PrunePlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-10T08:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new PrunePlantUseCaseHandler(
            plantRepository,
            careEventRepository,
            activeHouseholdPort,
            Clock.fixed(now, ZoneOffset.UTC));
    ReflectionTestUtils.setField(handler, "timezone", "UTC");
  }

  @Test
  void recordsPruningEventForToday() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    CareEvent event =
        handler.prune(
            new PrunePlantCommand(
                userId,
                plant.id(),
                LocalDate.parse("2026-01-10"),
                PruningKind.DRY_LEAVES,
                "liście"));

    ArgumentCaptor<CareEvent> captor = ArgumentCaptor.forClass(CareEvent.class);
    then(careEventRepository).should().save(captor.capture());
    assertThat(captor.getValue().careType()).isEqualTo(CareType.PRUNING);
    assertThat(captor.getValue().performedAt()).isEqualTo(now);
    assertThat(captor.getValue().performedBy()).isEqualTo(userId);
    assertThat(captor.getValue().pruningKind()).isEqualTo(PruningKind.DRY_LEAVES);
    assertThat(captor.getValue().notes()).isEqualTo("liście");
    assertThat(event).isEqualTo(captor.getValue());
  }

  @Test
  void recordsPastPruningAtNoonInZone() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    handler.prune(
        new PrunePlantCommand(
            userId, plant.id(), LocalDate.parse("2026-01-03"), PruningKind.HEAVY_PRUNING, null));

    ArgumentCaptor<CareEvent> captor = ArgumentCaptor.forClass(CareEvent.class);
    then(careEventRepository).should().save(captor.capture());
    assertThat(captor.getValue().performedAt())
        .isEqualTo(LocalDate.parse("2026-01-03").atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC));
    assertThat(captor.getValue().pruningKind()).isEqualTo(PruningKind.HEAVY_PRUNING);
  }

  @Test
  void futureDateThrows() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(
            () ->
                handler.prune(
                    new PrunePlantCommand(
                        userId, plant.id(), LocalDate.parse("2026-01-11"), null, null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("performedOn");
  }

  @Test
  void archivedPlantThrows() {
    Plant plant = plant(householdId).archive(now);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(
            () -> handler.prune(new PrunePlantCommand(userId, plant.id(), null, null, null)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant = plant(HouseholdId.newId());
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(
            () -> handler.prune(new PrunePlantCommand(userId, plant.id(), null, null, null)))
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

package com.crooked.florafatalis.plant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.in.ArchivePlantUseCase.ArchivePlantCommand;
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
class ArchivePlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private ArchivePlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-02T00:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new ArchivePlantUseCaseHandler(
            plantRepository, activeHouseholdPort, Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void archivesPlantInOwnHousehold() {
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

    handler.archive(new ArchivePlantCommand(userId, plant.id()));

    ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
    then(plantRepository).should().save(captor.capture());
    assertThat(captor.getValue().isArchived()).isTrue();
    assertThat(captor.getValue().archivedAt()).isEqualTo(now);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant =
        Plant.create(
            HouseholdId.newId(),
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            userId,
            now);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(() -> handler.archive(new ArchivePlantCommand(userId, plant.id())))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }
}

package com.crooked.florafatalis.plant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.plant.application.port.in.UpdatePlantUseCase.UpdatePlantCommand;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdatePlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private SpeciesRepository speciesRepository;
  @Mock private LocationRepository locationRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private UpdatePlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final LocationId locationId = LocationId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new UpdatePlantUseCaseHandler(
            plantRepository, speciesRepository, locationRepository, activeHouseholdPort);
  }

  @Test
  void updatesPlantInOwnHousehold() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));
    given(speciesRepository.findById(speciesId)).willReturn(Optional.of(species()));
    given(locationRepository.findById(locationId))
        .willReturn(
            Optional.of(new Location(locationId, householdId, "Salon", LocationKind.INDOOR)));

    handler.update(
        new UpdatePlantCommand(userId, plant.id(), speciesId, locationId, "Fikus", 5, null, null));

    ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
    then(plantRepository).should().save(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Fikus");
    assertThat(captor.getValue().wateringIntervalDaysOverride()).isEqualTo(5);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant = plant(HouseholdId.newId());
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(
            () ->
                handler.update(
                    new UpdatePlantCommand(
                        userId, plant.id(), speciesId, locationId, "Fikus", null, null, null)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  @Test
  void locationFromOtherHouseholdThrows() {
    Plant plant = plant(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));
    given(speciesRepository.findById(speciesId)).willReturn(Optional.of(species()));
    given(locationRepository.findById(locationId))
        .willReturn(
            Optional.of(
                new Location(locationId, HouseholdId.newId(), "Salon", LocationKind.INDOOR)));

    assertThatThrownBy(
            () ->
                handler.update(
                    new UpdatePlantCommand(
                        userId, plant.id(), speciesId, locationId, "Fikus", null, null, null)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  private Plant plant(HouseholdId household) {
    return Plant.create(
        household, speciesId, locationId, "Monstera", null, null, null, userId, now);
  }

  private Species species() {
    return new Species(
        speciesId, "Inny", null, 7, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null);
  }
}

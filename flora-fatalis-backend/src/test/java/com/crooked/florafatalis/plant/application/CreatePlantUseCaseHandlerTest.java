package com.crooked.florafatalis.plant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.plant.application.port.in.CreatePlantUseCase.CreatePlantCommand;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import com.crooked.florafatalis.species.domain.SpeciesNotFoundException;
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
class CreatePlantUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private SpeciesRepository speciesRepository;
  @Mock private LocationRepository locationRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private CreatePlantUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final LocationId locationId = LocationId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new CreatePlantUseCaseHandler(
            plantRepository,
            speciesRepository,
            locationRepository,
            activeHouseholdPort,
            Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void createsPlantInActiveHousehold() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(speciesRepository.findById(speciesId))
        .willReturn(
            Optional.of(
                new Species(speciesId, "Inny", null, 7, null, null, null, null, null, null, null)));
    given(locationRepository.findById(locationId))
        .willReturn(
            Optional.of(new Location(locationId, householdId, "Salon", LocationKind.INDOOR)));

    Plant created =
        handler.create(
            new CreatePlantCommand(userId, speciesId, locationId, "Monstera", null, null, null));

    ArgumentCaptor<Plant> captor = ArgumentCaptor.forClass(Plant.class);
    then(plantRepository).should().save(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Monstera");
    assertThat(captor.getValue().householdId()).isEqualTo(householdId);
    assertThat(captor.getValue().createdAt()).isEqualTo(now);
    assertThat(created).isEqualTo(captor.getValue());
  }

  @Test
  void noActiveHouseholdThrows() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                handler.create(
                    new CreatePlantCommand(
                        userId, speciesId, locationId, "Monstera", null, null, null)))
        .isInstanceOf(NoActiveHouseholdException.class);
  }

  @Test
  void unknownSpeciesThrows() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(speciesRepository.findById(speciesId)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                handler.create(
                    new CreatePlantCommand(
                        userId, speciesId, locationId, "Monstera", null, null, null)))
        .isInstanceOf(SpeciesNotFoundException.class);
  }

  @Test
  void locationFromOtherHouseholdThrows() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(speciesRepository.findById(speciesId))
        .willReturn(
            Optional.of(
                new Species(speciesId, "Inny", null, 7, null, null, null, null, null, null, null)));
    given(locationRepository.findById(locationId))
        .willReturn(
            Optional.of(
                new Location(locationId, HouseholdId.newId(), "Salon", LocationKind.INDOOR)));

    assertThatThrownBy(
            () ->
                handler.create(
                    new CreatePlantCommand(
                        userId, speciesId, locationId, "Monstera", null, null, null)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }
}

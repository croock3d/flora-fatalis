package com.crooked.florafatalis.photo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.photo.application.port.in.UploadPlantPhotoUseCase.UploadPlantPhotoCommand;
import com.crooked.florafatalis.photo.application.port.out.PhotoStorage;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
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
class UploadPlantPhotoUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;
  @Mock private PlantPhotoRepository plantPhotoRepository;
  @Mock private PhotoStorage photoStorage;

  private UploadPlantPhotoUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new UploadPlantPhotoUseCaseHandler(
            new PlantPhotoAccess(plantRepository, activeHouseholdPort),
            plantPhotoRepository,
            photoStorage,
            Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void firstPhotoBecomesPrimary() {
    Plant plant =
        Plant.create(
            householdId,
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            null,
            userId,
            now);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));
    given(plantPhotoRepository.findPrimaryByPlantId(plant.id())).willReturn(Optional.empty());

    PlantPhoto uploaded =
        handler.upload(
            new UploadPlantPhotoCommand(userId, plant.id(), "image/jpeg", new byte[] {1, 2, 3}));

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    then(photoStorage).should().store(keyCaptor.capture(), org.mockito.ArgumentMatchers.any());
    then(plantPhotoRepository).should().save(uploaded);
    assertThat(uploaded.primary()).isTrue();
    assertThat(keyCaptor.getValue()).contains(plant.id().value().toString());
  }

  @Test
  void rejectsUnsupportedType() {
    Plant plant =
        Plant.create(
            householdId,
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            null,
            userId,
            now);

    assertThatThrownBy(
            () ->
                handler.upload(
                    new UploadPlantPhotoCommand(userId, plant.id(), "image/gif", new byte[] {1})))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

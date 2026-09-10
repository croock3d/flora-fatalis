package com.crooked.florafatalis.photo.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.photo.application.port.out.PhotoStorage;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeletePlantPhotoUseCaseHandlerTest {

  @Mock private PlantPhotoRepository plantPhotoRepository;
  @Mock private PhotoStorage photoStorage;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private DeletePlantPhotoUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new DeletePlantPhotoUseCaseHandler(plantPhotoRepository, photoStorage, activeHouseholdPort);
  }

  @Test
  void deletesPhotoInOwnHousehold() {
    PlantPhoto photo = photo(householdId, false);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantPhotoRepository.findById(photo.id())).willReturn(Optional.of(photo));

    handler.delete(userId, photo.id());

    then(plantPhotoRepository).should().delete(photo.id());
    then(photoStorage).should().delete(photo.storageKey());
  }

  @Test
  void photoFromOtherHouseholdThrows() {
    PlantPhoto photo = photo(HouseholdId.newId(), true);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantPhotoRepository.findById(photo.id())).willReturn(Optional.of(photo));

    assertThatThrownBy(() -> handler.delete(userId, photo.id()))
        .isInstanceOf(HouseholdAccessDeniedException.class);
    then(plantPhotoRepository).should(never()).delete(photo.id());
    then(photoStorage).should(never()).delete(photo.storageKey());
  }

  private PlantPhoto photo(HouseholdId household, boolean primary) {
    return PlantPhoto.create(
        PlantId.newId(),
        household,
        household.value() + "/photo.jpg",
        "image/jpeg",
        Instant.parse("2026-01-01T00:00:00Z"),
        primary,
        0);
  }
}

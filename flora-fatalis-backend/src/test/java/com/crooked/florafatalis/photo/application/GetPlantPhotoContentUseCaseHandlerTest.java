package com.crooked.florafatalis.photo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.photo.application.port.in.GetPlantPhotoContentUseCase.PhotoContent;
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
class GetPlantPhotoContentUseCaseHandlerTest {

  @Mock private PlantPhotoRepository plantPhotoRepository;
  @Mock private PhotoStorage photoStorage;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private GetPlantPhotoContentUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new GetPlantPhotoContentUseCaseHandler(
            plantPhotoRepository, photoStorage, activeHouseholdPort);
  }

  @Test
  void returnsContentForOwnHousehold() {
    PlantPhoto photo = photo(householdId);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantPhotoRepository.findById(photo.id())).willReturn(Optional.of(photo));
    given(photoStorage.load(photo.storageKey())).willReturn(new byte[] {1, 2, 3});

    PhotoContent content = handler.get(userId, photo.id());

    assertThat(content.photo()).isEqualTo(photo);
    assertThat(content.bytes()).containsExactly(1, 2, 3);
  }

  @Test
  void photoFromOtherHouseholdThrows() {
    PlantPhoto photo = photo(HouseholdId.newId());
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantPhotoRepository.findById(photo.id())).willReturn(Optional.of(photo));

    assertThatThrownBy(() -> handler.get(userId, photo.id()))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  private PlantPhoto photo(HouseholdId household) {
    return PlantPhoto.create(
        PlantId.newId(),
        household,
        household.value() + "/photo.jpg",
        "image/jpeg",
        Instant.parse("2026-01-01T00:00:00Z"),
        true,
        0);
  }
}

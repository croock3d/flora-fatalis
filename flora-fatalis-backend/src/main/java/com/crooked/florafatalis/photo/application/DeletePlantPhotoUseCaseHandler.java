package com.crooked.florafatalis.photo.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.photo.application.port.in.DeletePlantPhotoUseCase;
import com.crooked.florafatalis.photo.application.port.out.PhotoStorage;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.photo.domain.PlantPhotoNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletePlantPhotoUseCaseHandler implements DeletePlantPhotoUseCase {

  private final PlantPhotoRepository plantPhotoRepository;
  private final PhotoStorage photoStorage;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public void delete(UserId userId, PlantPhotoId photoId) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(userId)
            .orElseThrow(NoActiveHouseholdException::new);
    PlantPhoto photo =
        plantPhotoRepository.findById(photoId).orElseThrow(PlantPhotoNotFoundException::new);
    if (!photo.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    plantPhotoRepository.delete(photo.id());
    photoStorage.delete(photo.storageKey());
    if (photo.primary()) {
      plantPhotoRepository.findByPlantId(photo.plantId()).stream()
          .findFirst()
          .ifPresent(next -> plantPhotoRepository.save(next.asPrimary()));
    }
  }
}

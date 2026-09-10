package com.crooked.florafatalis.photo.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.photo.application.port.in.SetPrimaryPlantPhotoUseCase;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.photo.domain.PlantPhotoNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SetPrimaryPlantPhotoUseCaseHandler implements SetPrimaryPlantPhotoUseCase {

  private final PlantPhotoRepository plantPhotoRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  @Transactional
  public void setPrimary(UserId userId, PlantPhotoId photoId) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(userId)
            .orElseThrow(NoActiveHouseholdException::new);
    PlantPhoto target =
        plantPhotoRepository.findById(photoId).orElseThrow(PlantPhotoNotFoundException::new);
    if (!target.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    plantPhotoRepository
        .findByPlantId(target.plantId())
        .forEach(
            photo -> {
              PlantPhoto updated =
                  photo.id().equals(target.id()) ? photo.asPrimary() : photo.asSecondary();
              plantPhotoRepository.save(updated);
            });
  }
}

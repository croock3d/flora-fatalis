package com.crooked.florafatalis.photo.application;

import com.crooked.florafatalis.photo.application.port.in.ListPlantPhotosUseCase;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPlantPhotosUseCaseHandler implements ListPlantPhotosUseCase {

  private final PlantPhotoAccess plantPhotoAccess;
  private final PlantPhotoRepository plantPhotoRepository;

  @Override
  public List<PlantPhoto> list(UserId userId, PlantId plantId) {
    plantPhotoAccess.requirePlant(userId, plantId);
    return plantPhotoRepository.findByPlantId(plantId);
  }
}

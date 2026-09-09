package com.crooked.florafatalis.photo.application.port.out;

import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.plant.domain.PlantId;
import java.util.List;
import java.util.Optional;

public interface PlantPhotoRepository {

  void save(PlantPhoto photo);

  Optional<PlantPhoto> findById(PlantPhotoId id);

  List<PlantPhoto> findByPlantId(PlantId plantId);

  Optional<PlantPhoto> findPrimaryByPlantId(PlantId plantId);

  void delete(PlantPhotoId id);
}

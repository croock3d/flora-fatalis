package com.crooked.florafatalis.plant.application;

import com.crooked.florafatalis.plant.application.port.in.ListPlantsUseCase;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPlantsUseCaseHandler implements ListPlantsUseCase {

  private final PlantRepository plantRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public List<Plant> list(UserId userId) {
    return activeHouseholdPort
        .findActiveHouseholdId(userId)
        .map(plantRepository::findActiveByHousehold)
        .orElse(List.of());
  }
}

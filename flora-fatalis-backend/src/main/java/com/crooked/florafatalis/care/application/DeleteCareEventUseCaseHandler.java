package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.DeleteCareEventUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareEventNotFoundException;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteCareEventUseCaseHandler implements DeleteCareEventUseCase {

  private final CareEventRepository careEventRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public void delete(DeleteCareEventCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    CareEvent event =
        careEventRepository
            .findById(command.eventId())
            .orElseThrow(CareEventNotFoundException::new);
    if (!event.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    careEventRepository.delete(event.id());
  }
}

package com.crooked.florafatalis.household.application.port.out;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;

public interface HouseholdInvitationRepository {

  void save(HouseholdInvitation invitation);

  Optional<HouseholdInvitation> findById(HouseholdInvitationId id);

  Optional<HouseholdInvitation> findByHouseholdId(HouseholdId householdId);

  Optional<HouseholdInvitation> findByInvitee(UserId inviteeId);

  void delete(HouseholdInvitationId id);
}

package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.adapter.repository.jpa.HouseholdInvitationEntity;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;

class HouseholdInvitationEntityMapper {

  static HouseholdInvitation toDomain(HouseholdInvitationEntity entity) {
    return new HouseholdInvitation(
        new HouseholdInvitationId(entity.getId()),
        new HouseholdId(entity.getHouseholdId()),
        new UserId(entity.getInviterId()),
        new UserId(entity.getInviteeId()),
        entity.getCreatedAt());
  }

  static HouseholdInvitationEntity toEntity(HouseholdInvitation invitation) {
    HouseholdInvitationEntity entity = new HouseholdInvitationEntity();
    entity.setId(invitation.id().value());
    entity.setHouseholdId(invitation.householdId().value());
    entity.setInviterId(invitation.inviterId().value());
    entity.setInviteeId(invitation.inviteeId().value());
    entity.setCreatedAt(invitation.createdAt());
    return entity;
  }
}

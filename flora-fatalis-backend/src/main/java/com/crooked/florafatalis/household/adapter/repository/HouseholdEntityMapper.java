package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.adapter.repository.jpa.HouseholdEntity;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;

class HouseholdEntityMapper {

  static Household toDomain(HouseholdEntity entity) {
    return new Household(
        new HouseholdId(entity.getId()),
        new UserId(entity.getOwnerId()),
        entity.getPartnerId() == null ? null : new UserId(entity.getPartnerId()),
        entity.getCreatedAt());
  }

  static HouseholdEntity toEntity(Household household) {
    HouseholdEntity entity = new HouseholdEntity();
    entity.setId(household.id().value());
    entity.setOwnerId(household.ownerId().value());
    entity.setPartnerId(household.partnerId() == null ? null : household.partnerId().value());
    entity.setCreatedAt(household.createdAt());
    return entity;
  }
}

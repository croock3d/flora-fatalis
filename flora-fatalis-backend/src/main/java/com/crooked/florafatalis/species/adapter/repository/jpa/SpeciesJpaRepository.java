package com.crooked.florafatalis.species.adapter.repository.jpa;

import com.crooked.florafatalis.species.adapter.repository.jpa.entity.SpeciesEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesJpaRepository extends JpaRepository<SpeciesEntity, UUID> {}

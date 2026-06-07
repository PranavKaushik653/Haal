package com.haal.backend.safety.repository;

import com.haal.backend.safety.entity.SosEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SosEventRepository extends JpaRepository<SosEvent, UUID> {

    Optional<SosEvent> findTopByUserIdAndResolvedFalse(UUID userId);

    boolean existsByUserIdAndResolvedFalse(UUID userId);
}

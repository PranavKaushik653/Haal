package com.haal.backend.social.repository;


import com.haal.backend.social.entity.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, UUID> {
    Optional<Connection> findByUserIdAndOtherUserId(UUID userId, UUID otherUserId);

    boolean existsByUserIdAndOtherUserId(UUID userId, UUID otherUserId);

    List<Connection> findByUserId(UUID userId);

    long countByUserId(UUID userId);
}

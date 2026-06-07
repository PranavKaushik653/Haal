package com.haal.backend.safety.repository;

import com.haal.backend.safety.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    Optional<CheckIn> findTopByUserIdOrderByCheckedInAtDesc(UUID userId);

    // Scheduler query: users who haven't checked in AND alert not yet sent
    @Query("""
        SELECT c FROM CheckIn c
        WHERE c.alertSent = false
        AND c.checkedInAt < :cutoff
        AND c.id IN (
            SELECT MAX(c2.id) FROM CheckIn c2
            GROUP BY c2.userId
        )
    """)
    List<CheckIn> findOverdueCheckIns(Instant cutoff);

    boolean existsByUserIdAndCheckedInAtAfter(UUID userId, Instant after);
}

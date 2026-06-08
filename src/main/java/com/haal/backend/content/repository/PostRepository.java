package com.haal.backend.content.repository;

import com.haal.backend.content.entity.ModerationStatus;
import com.haal.backend.content.entity.Post;
import com.haal.backend.content.entity.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<Post> findByVisibilityAndModerationStatusOrderByCreatedAtDesc(
            PostVisibility visibility, ModerationStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Post p
        WHERE p.visibility = 'PUBLIC'
        AND p.moderationStatus = 'APPROVED'
        AND p.location IS NOT NULL
        AND ST_DWithin(p.location, :userLocation, 10000) = true
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findNearbyPosts(
            org.locationtech.jts.geom.Point userLocation,
            Pageable pageable);

    List<Post> findByModerationStatusOrderByCreatedAtAsc(ModerationStatus status);

    Optional<Post> findByIdAndUserId(UUID postId, UUID userId);
}




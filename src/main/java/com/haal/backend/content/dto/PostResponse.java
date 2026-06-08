package com.haal.backend.content.dto;

import com.haal.backend.content.entity.ModerationStatus;
import com.haal.backend.content.entity.PostVisibility;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PostResponse {
    private UUID id;
    private String anonymousAuthorAlias;
    private String title;
    private String content;
    private PostVisibility visibility;
    private Double distanceKm;          // For proximity feed
    private Long likesCount;
    private boolean likedByCurrentUser;
    private Instant createdAt;
    private ModerationStatus moderationStatus;
}

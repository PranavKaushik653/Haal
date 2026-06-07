package com.haal.backend.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String anonymousAlias;
    private Instant createdAt;
    private boolean active;
}

package com.haal.backend.social.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NearbyUserResponse {
    private UUID userId;
    private String anonymousAlias;
    private Double distanceKm;
    private Instant lastCheckIn;
    private boolean isConnected;
}
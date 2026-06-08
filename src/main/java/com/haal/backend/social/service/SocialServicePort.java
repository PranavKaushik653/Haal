package com.haal.backend.social.service;

import com.haal.backend.social.dto.NearbyUserResponse;

import java.util.List;
import java.util.UUID;

public interface SocialServicePort {
    List<NearbyUserResponse> discoverNearby(UUID userId, Double latitude,
                                            Double longitude, int radiusKm);

    void connect(UUID userId, UUID otherUserId);

    void disconnect(UUID userId, UUID otherUserId);

    List<NearbyUserResponse> getConnections(UUID userId);

    long getConnectionCount(UUID userId);
}

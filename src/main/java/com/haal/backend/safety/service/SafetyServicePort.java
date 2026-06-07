package com.haal.backend.safety.service;

import com.haal.backend.safety.dto.CheckInResponse;
import com.haal.backend.safety.dto.SosRequest;
import com.haal.backend.safety.dto.SosResponse;

import java.util.UUID;

public interface SafetyServicePort {
    CheckInResponse checkIn(UUID userId, Double latitude, Double longitude, String ipAddress);
    SosResponse triggerSos(UUID userId, SosRequest request);
    void resolveSos(UUID userId);

}

package com.haal.backend.safety.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SosResponse {
    private UUID sosEventId;
    private Instant triggeredAt;
    private String message;      // "Emergency contacts have been notified"
}

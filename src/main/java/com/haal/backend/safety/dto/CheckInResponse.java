package com.haal.backend.safety.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CheckInResponse {
    private UUID checkInId;
    private Instant checkedInAt;
    private Instant nextCheckInDeadline;
    private boolean locationRecorded;
}

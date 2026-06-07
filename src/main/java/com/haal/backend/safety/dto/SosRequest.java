package com.haal.backend.safety.dto;

import lombok.Data;

@Data
public class SosRequest {
    private Double latitude;     // nullable — GPS may not be available
    private Double longitude;
    private String notes;        // optional: "I'm at the red building"
}

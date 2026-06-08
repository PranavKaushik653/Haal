package com.haal.backend.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Alias is required")
    @Size(min = 3, max = 50, message = "Alias must be 3–50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Alias can only contain letters, numbers, underscores")
    private String anonymousAlias;

    @NotBlank(message = "Phone is required")
    // ← FIXED: Allows 1-14 digits after country code (E.164 compliant)
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Phone must be in E.164 format (e.g. +919876543210)")
    private String phone;

    @NotBlank(message = "Emergency contact name is required")
    @Size(min = 2, max = 100)
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Emergency contact phone must be in E.164 format")
    private String emergencyContactPhone;

    @Email(message = "Emergency contact email must be valid")
    private String emergencyContactEmail;
}
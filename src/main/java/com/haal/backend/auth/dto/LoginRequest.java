package com.haal.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$",  // ← FIXED
            message = "Phone must be in E.164 format (e.g. +919876543210)")
    private String phone;
}

package com.haal.backend.auth.controller;

import com.haal.backend.auth.dto.*;
import com.haal.backend.auth.entity.User;
import com.haal.backend.auth.security.HaalUserDetails;
import com.haal.backend.auth.service.AuthServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, and token management")
public class AuthController {

    private final AuthServicePort authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user with anonymous alias + phone")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with phone number")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile — no PII returned")
    public ResponseEntity<UserProfileResponse> me(
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .anonymousAlias(user.getAnonymousAlias())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .build());
    }
}

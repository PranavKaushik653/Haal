package com.haal.backend.social.controller;

import com.haal.backend.auth.security.HaalUserDetails;
import com.haal.backend.social.dto.NearbyUserResponse;
import com.haal.backend.social.service.SocialServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
@Tag(name = "Social", description = "Discovery and connections")
public class SocialController {

    private final SocialServicePort socialService;

    @GetMapping("/nearby")
    @Operation(summary = "Discover users within 10km (online in last 24h)")
    public ResponseEntity<List<NearbyUserResponse>> discoverNearby(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") int radiusKm) {
        return ResponseEntity.ok(socialService.discoverNearby(
                userDetails.getUserId(), latitude, longitude, radiusKm));
    }

    @PostMapping("/connect/{otherUserId}")
    @Operation(summary = "Add user to connections")
    public ResponseEntity<Void> connect(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @PathVariable String otherUserId) {
        socialService.connect(userDetails.getUserId(), UUID.fromString(otherUserId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/disconnect/{otherUserId}")
    @Operation(summary = "Remove user from connections")
    public ResponseEntity<Void> disconnect(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @PathVariable String otherUserId) {
        socialService.disconnect(userDetails.getUserId(), UUID.fromString(otherUserId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/connections")
    @Operation(summary = "Get current user's connections")
    public ResponseEntity<List<NearbyUserResponse>> getConnections(
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        return ResponseEntity.ok(socialService.getConnections(userDetails.getUserId()));
    }

    @GetMapping("/connections/count")
    @Operation(summary = "Get connection count")
    public ResponseEntity<Long> getConnectionCount(
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        return ResponseEntity.ok(socialService.getConnectionCount(userDetails.getUserId()));
    }
}

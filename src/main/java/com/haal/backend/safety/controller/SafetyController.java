package com.haal.backend.safety.controller;

import com.haal.backend.auth.security.HaalUserDetails;
import com.haal.backend.safety.dto.CheckInResponse;
import com.haal.backend.safety.dto.SosRequest;
import com.haal.backend.safety.dto.SosResponse;
import com.haal.backend.safety.service.SafetyServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/safety")
@RequiredArgsConstructor
@Tag(name = "Safety", description = "Check-in and SOS endpoints")
public class SafetyController {

    private final SafetyServicePort safetyService;

    @PostMapping("/checkin")
    @Operation(summary = "Record a check-in — resets the 48h Dead Man's Switch")
    public ResponseEntity<CheckInResponse> checkIn(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                safetyService.checkIn(
                        userDetails.getUserId(),
                        latitude,
                        longitude,
                        httpRequest.getRemoteAddr()
                )
        );
    }

    @PostMapping("/sos")
    @Operation(summary = "Trigger SOS panic button — immediately alerts emergency contacts")
    public ResponseEntity<SosResponse> triggerSos(
            @AuthenticationPrincipal HaalUserDetails userDetails,
            @RequestBody(required = false) SosRequest request) {

        if (request == null) request = new SosRequest();
        return ResponseEntity.ok(
                safetyService.triggerSos(userDetails.getUserId(), request)
        );
    }

    @PostMapping("/sos/resolve")
    @Operation(summary = "Mark active SOS as resolved")
    public ResponseEntity<Void> resolveSos(
            @AuthenticationPrincipal HaalUserDetails userDetails) {
        safetyService.resolveSos(userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}

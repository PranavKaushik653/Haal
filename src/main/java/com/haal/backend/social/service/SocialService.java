package com.haal.backend.social.service;

import com.haal.backend.auth.repository.UserRepository;
import com.haal.backend.safety.repository.CheckInRepository;
import com.haal.backend.social.entity.Connection;
import com.haal.backend.social.repository.ConnectionRepository;
import com.haal.backend.social.dto.NearbyUserResponse;
import com.haal.backend.shared.exception.ErrorCode;
import com.haal.backend.shared.exception.HaalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialService implements SocialServicePort {

    private final ConnectionRepository connectionRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NearbyUserResponse> discoverNearby(UUID userId, Double latitude,
                                                   Double longitude, int radiusKm) {
        // Get all users who checked in within the last 24 hours
        Instant recentCutoff = Instant.now().minus(24, ChronoUnit.HOURS);

        // This is a simplified version — in production, use raw SQL with PostGIS
        // For now, just get all recent check-ins
        var recentCheckIns = checkInRepository.findAll().stream()
                .filter(ci -> ci.getCheckedInAt().isAfter(recentCutoff))
                .filter(ci -> !ci.getUserId().equals(userId))
                .collect(Collectors.toList());

        return recentCheckIns.stream()
                .map(ci -> {
                    var user = userRepository.findById(ci.getUserId()).orElse(null);
                    if (user == null) return null;

                    boolean isConnected = connectionRepository
                            .existsByUserIdAndOtherUserId(userId, ci.getUserId());

                    return NearbyUserResponse.builder()
                            .userId(ci.getUserId())
                            .anonymousAlias(user.getAnonymousAlias())
                            .distanceKm(0.0)  // TODO: calculate from PostGIS
                            .lastCheckIn(ci.getCheckedInAt())
                            .isConnected(isConnected)
                            .build();
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void connect(UUID userId, UUID otherUserId) {
        if (userId.equals(otherUserId)) {
            throw HaalException.badRequest(ErrorCode.VALIDATION_FAILED);
        }

        userRepository.findById(otherUserId)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        if (!connectionRepository.existsByUserIdAndOtherUserId(userId, otherUserId)) {
            Connection conn = Connection.builder()
                    .userId(userId)
                    .otherUserId(otherUserId)
                    .build();
            connectionRepository.save(conn);
            log.info("Users connected: {} -> {}", userId, otherUserId);
        }
    }

    @Override
    @Transactional
    public void disconnect(UUID userId, UUID otherUserId) {
        connectionRepository.findByUserIdAndOtherUserId(userId, otherUserId)
                .ifPresent(conn -> {
                    connectionRepository.delete(conn);
                    log.info("Users disconnected: {} -> {}", userId, otherUserId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<NearbyUserResponse> getConnections(UUID userId) {
        return connectionRepository.findByUserId(userId).stream()
                .map(conn -> {
                    var user = userRepository.findById(conn.getOtherUserId()).orElse(null);
                    if (user == null) return null;

                    return NearbyUserResponse.builder()
                            .userId(conn.getOtherUserId())
                            .anonymousAlias(user.getAnonymousAlias())
                            .distanceKm(0.0)
                            .lastCheckIn(conn.getConnectedAt())
                            .isConnected(true)
                            .build();
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getConnectionCount(UUID userId) {
        return connectionRepository.countByUserId(userId);
    }
}

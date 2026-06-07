package com.haal.backend.safety.service;

import com.haal.backend.safety.dto.CheckInResponse;
import com.haal.backend.safety.dto.SosRequest;
import com.haal.backend.safety.dto.SosResponse;
import com.haal.backend.safety.entity.CheckIn;
import com.haal.backend.safety.entity.SosEvent;
import com.haal.backend.safety.repository.CheckInRepository;
import com.haal.backend.safety.repository.SosEventRepository;
import com.haal.backend.shared.config.AppProperties;
import com.haal.backend.shared.exception.ErrorCode;
import com.haal.backend.shared.exception.HaalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyService implements SafetyServicePort {

    private final CheckInRepository checkInRepository;
    private final SosEventRepository sosEventRepository;
    private final AlertServicePort   alertService;
    private final AppProperties appProperties;

    // WGS84 SRID — must match PostGIS column definition
    private static final int SRID = 4326;
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), SRID);

    @Override
    @Transactional
    public CheckInResponse checkIn(UUID userId, Double latitude,
                                   Double longitude, String ipAddress) {
        Instant now = Instant.now();

        Point location = buildPoint(latitude, longitude);

        CheckIn checkIn = CheckIn.builder()
                .userId(userId)
                .checkedInAt(now)
                .location(location)
                .ipAddress(ipAddress)
                .build();

        checkInRepository.save(checkIn);
        log.info("Check-in recorded for user={}", userId);

        int windowHours = appProperties.getSafety().getCheckinWindowHours();
        Instant deadline = now.plus(windowHours, ChronoUnit.HOURS);

        return CheckInResponse.builder()
                .checkInId(checkIn.getId())
                .checkedInAt(now)
                .nextCheckInDeadline(deadline)
                .locationRecorded(location != null)
                .build();
    }

    @Override
    @Transactional
    public SosResponse triggerSos(UUID userId, SosRequest request) {
        // Prevent duplicate active SOS events
        if (sosEventRepository.existsByUserIdAndResolvedFalse(userId)) {
            throw HaalException.conflict(ErrorCode.SOS_ALREADY_ACTIVE);
        }

        Point location = buildPoint(request.getLatitude(), request.getLongitude());

        SosEvent sos = SosEvent.builder()
                .userId(userId)
                .triggeredAt(Instant.now())
                .location(location)
                .notes(request.getNotes())
                .build();

        sos = sosEventRepository.save(sos);

        // Alert is synchronous here — for a panic button, speed matters
        // In Phase 4, this moves to an async event
        alertService.sendSosAlert(sos);

        log.warn("SOS triggered for user={}", userId);

        return SosResponse.builder()
                .sosEventId(sos.getId())
                .triggeredAt(sos.getTriggeredAt())
                .message("Emergency contacts have been notified. Help is coming.")
                .build();
    }

    @Override
    @Transactional
    public void resolveSos(UUID userId) {
        sosEventRepository.findTopByUserIdAndResolvedFalse(userId)
                .ifPresent(sos -> {
                    sos.setResolved(true);
                    sos.setResolvedAt(Instant.now());
                    sosEventRepository.save(sos);
                    log.info("SOS resolved for user={}", userId);
                });
    }

    private Point buildPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;
        // JTS: x = longitude, y = latitude
        Coordinate coord = new Coordinate(longitude, latitude);
        return geometryFactory.createPoint(coord);
    }
}
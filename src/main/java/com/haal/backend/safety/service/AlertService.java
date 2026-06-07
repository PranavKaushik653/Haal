package com.haal.backend.safety.service;

import com.haal.backend.auth.entity.User;
import com.haal.backend.auth.repository.UserRepository;
import com.haal.backend.safety.entity.CheckIn;
import com.haal.backend.safety.entity.SosEvent;
import com.haal.backend.safety.repository.CheckInRepository;
import com.haal.backend.shared.config.PiiEncryptionService;
import com.haal.backend.shared.exception.ErrorCode;
import com.haal.backend.shared.exception.HaalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService implements AlertServicePort {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final PiiEncryptionService piiEncryption;

    @Override
    @Transactional
    public void sendMissedCheckInAlert(CheckIn overdueCheckIn) {
        User user = userRepository.findById(overdueCheckIn.getUserId())
                .orElseThrow(() ->
                        HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        // Decrypt PII only at the moment of sending — not stored in memory
        String ecEmail = user.getEcEmailEncrypted() != null
                ? piiEncryption.decrypt(user.getEcEmailEncrypted())
                : null;

        String ecName  = piiEncryption.decrypt(user.getEcNameEncrypted());
        String alias   = user.getAnonymousAlias();

        String lastSeen = DateTimeFormatter
                .ofPattern("dd MMM yyyy, HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC)
                .format(overdueCheckIn.getCheckedInAt());

        if (ecEmail != null) {
            sendEmail(
                    ecEmail,
                    "[Haal] Wellness Check — " + alias + " hasn't checked in",
                    buildMissedCheckInBody(ecName, alias, lastSeen)
            );
        }

        // Mark alert sent — idempotency gate
        overdueCheckIn.setAlertSent(true);
        overdueCheckIn.setAlertSentAt(Instant.now());
        checkInRepository.save(overdueCheckIn);

        log.info("Missed check-in alert sent for user={}", alias);
    }
    @Override
    public void sendSosAlert(SosEvent sosEvent) {
        User user = userRepository.findById(sosEvent.getUserId())
                .orElseThrow(() ->
                        HaalException.notFound(ErrorCode.USER_NOT_FOUND));

        String ecEmail = user.getEcEmailEncrypted() != null
                ? piiEncryption.decrypt(user.getEcEmailEncrypted())
                : null;

        String ecName  = piiEncryption.decrypt(user.getEcNameEncrypted());
        String alias   = user.getAnonymousAlias();

        if (ecEmail != null) {
            sendEmail(
                    ecEmail,
                    "🚨 [Haal] SOS ALERT — " + alias + " needs help",
                    buildSosBody(ecName, alias, sosEvent)
            );
        }

        log.warn("SOS alert sent for user={}", alias);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Email dispatch failed to={}", to, e);
            // Don't rethrow — alert failure should not crash the scheduler
        }
    }

    private String buildMissedCheckInBody(String ecName, String alias,
                                          String lastSeen) {
        return """
            Hi %s,
            
            This is an automated wellness check from Haal.
            
            Your contact "%s" has not checked in since %s (over 48 hours ago).
            
            This could mean:
            - They forgot to check in (most likely)
            - They may need help
            
            Please try reaching them through your usual channels.
            
            If you cannot reach them and are concerned for their safety,
            please contact local emergency services.
            
            — The Haal Safety System
            """.formatted(ecName, alias, lastSeen);
    }

    private String buildSosBody(String ecName, String alias, SosEvent sos) {
        String location = (sos.getLocation() != null)
                ? "Lat: %.6f, Lon: %.6f".formatted(
                sos.getLocation().getY(),
                sos.getLocation().getX())
                : "Location not available";

        return """
            Hi %s,
            
            🚨 EMERGENCY ALERT from Haal 🚨
            
            Your contact "%s" has triggered the SOS panic button.
            
            Time: %s
            Last known location: %s
            
            Please contact them immediately.
            If you cannot reach them, contact emergency services (112/911).
            
            — The Haal Safety System
            """.formatted(ecName, alias,
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm 'UTC'")
                        .withZone(ZoneOffset.UTC)
                        .format(sos.getTriggeredAt()),
                location);
    }
}

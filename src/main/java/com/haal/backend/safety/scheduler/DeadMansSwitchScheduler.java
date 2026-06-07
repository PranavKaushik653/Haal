package com.haal.backend.safety.scheduler;

import com.haal.backend.safety.entity.CheckIn;
import com.haal.backend.safety.repository.CheckInRepository;
import com.haal.backend.safety.service.AlertServicePort;
import com.haal.backend.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadMansSwitchScheduler {

    private final CheckInRepository checkInRepository;
    private final AlertServicePort alertService;
    private final AppProperties appProperties;

    /**
     * Runs every 15 minutes (configurable).
     * Finds users whose latest check-in is beyond the 48h window
     * and who haven't been alerted yet.
     *
     * IDEMPOTENT: alert_sent flag prevents double-sending even if
     * the scheduler fires twice (e.g., after restart).
     */
    @Scheduled(cron = "${haal.safety.scheduler-cron}")
    @Transactional
    public void checkForOverdueUsers() {
        int windowHours = appProperties.getSafety().getCheckinWindowHours();
        Instant cutoff  = Instant.now().minus(windowHours, ChronoUnit.HOURS);

        List<CheckIn> overdueCheckIns =
                checkInRepository.findOverdueCheckIns(cutoff);

        if (overdueCheckIns.isEmpty()) {
            log.debug("Dead man's switch: no overdue users found");
            return;
        }

        log.warn("Dead man's switch: {} overdue user(s) found",
                overdueCheckIns.size());

        for (CheckIn checkIn : overdueCheckIns) {
            try {
                alertService.sendMissedCheckInAlert(checkIn);
            } catch (Exception e) {
                // Per-user failure isolation — one failure doesn't skip others
                log.error("Alert failed for checkIn={}: {}",
                        checkIn.getId(), e.getMessage());
            }
        }
    }
}

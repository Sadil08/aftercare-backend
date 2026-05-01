package com.aftercare.aftercare_portal.service;

import com.aftercare.aftercare_portal.entity.DeathCase;
import com.aftercare.aftercare_portal.repository.DeathCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs daily at 9am on weekdays and logs any death cases that have been
 * open (non-terminal) for more than 30 days without a status change.
 * In production this would send email/SMS alerts to the relevant admin or sector.
 */
@Component
public class CaseTimeoutAlertJob {

    private static final Logger log = LoggerFactory.getLogger(CaseTimeoutAlertJob.class);
    private static final int STALE_THRESHOLD_DAYS = 30;

    private final DeathCaseRepository deathCaseRepository;

    public CaseTimeoutAlertJob(DeathCaseRepository deathCaseRepository) {
        this.deathCaseRepository = deathCaseRepository;
    }

    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void checkStaleCases() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(STALE_THRESHOLD_DAYS);
        List<DeathCase> staleCases = deathCaseRepository.findStaleCasesOpenBefore(cutoff);

        if (staleCases.isEmpty()) {
            log.info("Case timeout check: no stale cases found.");
            return;
        }

        log.warn("STALE CASES DETECTED: {} case(s) have been open for >{} days",
                staleCases.size(), STALE_THRESHOLD_DAYS);
        for (DeathCase dc : staleCases) {
            log.warn("  [Case #{}] status={} | sector={} | created={}",
                    dc.getId(), dc.getStatus(), dc.getSector().getCode(), dc.getCreatedAt());
        }
    }
}

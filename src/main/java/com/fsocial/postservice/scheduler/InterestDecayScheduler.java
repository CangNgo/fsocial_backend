package com.fsocial.postservice.scheduler;

import com.fsocial.postservice.services.InterestGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Applies daily decay to user interest weights.
 * BRD: weight_new = weight_old × 0.95, run at 2:00 AM every day.
 * Entries with weight < 0.1 are removed to avoid noise accumulation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterestDecayScheduler {

    private static final double DECAY_FACTOR = 0.95;
    private static final double REMOVAL_THRESHOLD = 0.1;

    private final InterestGraphService interestGraphService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void applyDailyDecay() {
        log.info("Starting daily interest decay job");
        try {
            interestGraphService.applyDecay(DECAY_FACTOR, REMOVAL_THRESHOLD);
            log.info("Daily interest decay job completed");
        } catch (Exception e) {
            log.error("Daily interest decay job failed: {}", e.getMessage(), e);
        }
    }
}

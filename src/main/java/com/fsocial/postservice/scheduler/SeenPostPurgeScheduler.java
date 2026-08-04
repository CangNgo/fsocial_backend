package com.fsocial.postservice.scheduler;

import com.fsocial.postservice.repository.SeenPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Postgres không có TTL index như Mongo — dọn seen_post bằng job hằng ngày.
 * Giữ 14 ngày, đúng bằng TTL cũ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeenPostPurgeScheduler {

    private static final int RETENTION_DAYS = 14;

    private final SeenPostRepository seenPostRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeSeenPosts() {
        try {
            int removed = seenPostRepository.deleteBySeenAtBefore(LocalDateTime.now().minusDays(RETENTION_DAYS));
            log.info("Purged {} seen_post rows older than {} days", removed, RETENTION_DAYS);
        } catch (Exception e) {
            log.error("Seen post purge job failed: {}", e.getMessage(), e);
        }
    }
}

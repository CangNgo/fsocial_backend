package com.fsocial.postservice.scheduler;

import com.fsocial.postservice.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Đồng bộ định kỳ bảng hashtag catalog từ post.tags — full resync, đơn giản hơn giữ delta. */
@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagSyncScheduler {

    private final HashtagRepository hashtagRepository;

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void syncHashtagCatalog() {
        try {
            hashtagRepository.syncCounts();
            hashtagRepository.deleteUnused();
            log.info("Hashtag catalog sync completed");
        } catch (Exception e) {
            log.error("Hashtag catalog sync failed: {}", e.getMessage(), e);
        }
    }
}

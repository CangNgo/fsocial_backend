package com.fsocial.postservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Creates MongoDB indexes that cannot be expressed purely via annotations,
 * specifically the TTL index on seen_posts.seen_at (30 days expiry).
 *
 * Runs once at startup — safe to call repeatedly (MongoDB ignores duplicate index definitions).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoIndexInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureSeenPostTtlIndex();
        log.info("MongoDB index initialization complete");
    }

    private void ensureSeenPostTtlIndex() {
        try {
            IndexOperations ops = mongoTemplate.indexOps("seen_posts");
            ops.ensureIndex(new Index()
                    .on("seen_at", Sort.Direction.ASC)
                    .expire(30, TimeUnit.DAYS));
            log.debug("TTL index ensured on seen_posts.seen_at (30 days)");
        } catch (Exception e) {
            log.warn("Could not ensure TTL index on seen_posts: {}", e.getMessage());
        }
    }
}

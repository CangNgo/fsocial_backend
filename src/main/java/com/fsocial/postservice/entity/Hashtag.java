package com.fsocial.postservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Catalog hashtag, đồng bộ định kỳ từ post.tags qua HashtagSyncScheduler
 * (không upsert lúc tạo/share post để tránh thêm write-path overhead).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "hashtag")
public class Hashtag {

    @Id
    @Column(name = "name", length = 64)
    String name;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    int usageCount = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();
}

package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * Ghi lại từng lượt xem FAQ (1 dòng / 1 lượt xem) thay vì đếm dồn trên Faq,
 * để thống kê view chính xác và tránh lệch số khi cập nhật song song.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "faq_view")
public class FaqView {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
    String id;

    @Column(name = "faq_id", length = 36, nullable = false)
    String faqId;

    @Column(name = "user_id", length = 36)
    String userId;

    @Column(name = "viewed_at", nullable = false)
    @Builder.Default
    LocalDateTime viewedAt = LocalDateTime.now();
}

package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Đánh giá của 1 user cho 1 FAQ (mỗi user chỉ 1 đánh giá / FAQ, đánh giá lại sẽ ghi đè).
 * Điểm trung bình/số lượt đánh giá tính bằng aggregate query trên bảng này để đảm bảo chính xác.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "faq_rating")
@IdClass(FaqRating.Key.class)
public class FaqRating {

    @Id
    @Column(name = "faq_id", length = 36)
    String faqId;

    @Id
    @Column(name = "user_id", length = 36)
    String userId;

    @Column(name = "score", nullable = false)
    Integer score;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String faqId;
        private String userId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key)) return false;
            return Objects.equals(faqId, key.faqId) && Objects.equals(userId, key.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(faqId, userId);
        }
    }
}

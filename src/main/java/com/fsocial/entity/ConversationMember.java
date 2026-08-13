package com.fsocial.entity;

import com.fsocial.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "conversation_members", uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"}))
public class ConversationMember extends AbstractEntity<String> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    Conversation conversation;

    @Column(name = "user_id", length = 36, nullable = false)
    String userId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", length = 16, nullable = false)
    MemberRole role = MemberRole.MEMBER;

    @Column(name = "last_read_at")
    LocalDateTime lastReadAt;

    @Builder.Default
    @Column(name = "is_muted", nullable = false)
    boolean muted = false;

    @Builder.Default
    @Column(name = "joined_at")
    LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "left_at")
    LocalDateTime leftAt;
}

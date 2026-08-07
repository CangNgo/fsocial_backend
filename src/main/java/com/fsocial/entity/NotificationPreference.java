package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter @SuperBuilder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "notification_preference")
public class NotificationPreference extends AbstractEntity<String> {

    @Column(name = "user_id", length = 36, nullable = false, unique = true)
    private String userId;

    /** Key là tên NotificationType (LIKE, COMMENT...), value là setting per-channel */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "preference_setting",
            joinColumns = @JoinColumn(name = "preference_id"))
    @MapKeyColumn(name = "notification_type", length = 32)
    @Builder.Default
    private Map<String, ChannelSettings> settings = new HashMap<>();

    @Column(name = "quiet_hours_start", length = 8)
    private String quietHoursStart;     // "22:00"

    @Column(name = "quiet_hours_end", length = 8)
    private String quietHoursEnd;       // "07:00"

    @Column(name = "timezone", length = 64)
    private String timezone;            // "Asia/Ho_Chi_Minh"
}

package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_template")
public class NotificationTemplate extends AbstractEntity<String> {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false, unique = true)
    private NotificationType type;

    /** Map locale ("vi", "en", "ja"...) → template tương ứng */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "template_translation",
            joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "locale", length = 8)
    @Builder.Default
    private Map<String, LocalizedTemplate> translations = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "template_default_data",
            joinColumns = @JoinColumn(name = "template_id"))
    @MapKeyColumn(name = "data_key", length = 64)
    @Column(name = "data_value", columnDefinition = "text")
    @Builder.Default
    private Map<String, String> defaultData = new HashMap<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}

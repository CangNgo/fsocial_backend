package com.fsocial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChannelSettings {

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default private boolean pushEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default private boolean emailEnabled = false;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default private boolean inAppEnabled = true;
}

package com.fsocial.postservice.entity;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSettings {
    @Builder.Default private boolean pushEnabled  = true;
    @Builder.Default private boolean emailEnabled = false;
    @Builder.Default private boolean inAppEnabled = true;
}

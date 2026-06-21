package com.fsocial.postservice.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorSnapshot {
    private String userId;
    private String username;
    private String displayName;
    private String avatarUrl;
}

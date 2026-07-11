package com.fsocial.postservice.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorSnapshot {
    @Field("user_id")
    private String userId;
    @Field("display_name")
    private String displayName;
    private String avatar;
}

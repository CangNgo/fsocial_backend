package com.fsocial.postservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActorSnapshotDTO {
    @Id
    String userId;
    String displayName;
    String avatar;
}

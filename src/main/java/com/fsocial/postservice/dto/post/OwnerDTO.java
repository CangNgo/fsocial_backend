package com.fsocial.postservice.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnerDTO {
    String userId;
    String displayName;
    String avatar;
}

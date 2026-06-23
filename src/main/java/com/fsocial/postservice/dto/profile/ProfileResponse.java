package com.fsocial.postservice.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    String id;
    String avatar;
    String bio;
    String background;
    String displayName;
    int gender;
}

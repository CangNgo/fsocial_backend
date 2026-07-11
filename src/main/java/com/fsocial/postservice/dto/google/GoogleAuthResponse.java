package com.fsocial.postservice.dto.google;

import java.time.LocalDate;

public record GoogleAuthResponse(
        String accessToken,
        String tokenType,
        UserInfo user
) {
    public record UserInfo(
            String id,
            String email,
            String displayName,
            String givenName,
            String familyName,
            String picture,
            String locale,
            LocalDate birthday    // mới thêm
    ) {}
}

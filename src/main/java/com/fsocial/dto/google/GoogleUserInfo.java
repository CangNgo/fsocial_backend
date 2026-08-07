package com.fsocial.dto.google;

public record GoogleUserInfo(
        String googleId, String email,
        String displayName,
        String givenName,
        String familyName,
        String picture,
        String locale
) {}
package com.fsocial.services;

import com.fsocial.dto.response.AuthenticationResponse;
import com.fsocial.entity.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username, String userAgent, String ipAddress);
    RefreshToken validRefreshToken(String token, String userAgent, String ipAddress);
    AuthenticationResponse refreshAccessToken(String refreshToken, String userAgent, String ipAddress);
    void disableRefreshToken(String refreshToken);
}

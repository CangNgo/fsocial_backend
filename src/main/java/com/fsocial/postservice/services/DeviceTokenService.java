package com.fsocial.postservice.services;

import java.util.List;

public interface DeviceTokenService {
    void registerToken (String userId, String token, String deviceType);
    List<String > getTokenByUserId (String userId);
    void removeInvalidToken(String token) ;
}

package com.fsocial.postservice.services;

public interface JwtService {
    String generateToken(String username);
    boolean verifyToken(String token);
    byte[] getSignerKey();
    String getUserId(String token);
    String getToken(String authorization);
}

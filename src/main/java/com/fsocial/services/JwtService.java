package com.fsocial.services;

public interface JwtService {
    String generateToken(String accountId);
    boolean verifyToken(String token);
    byte[] getSignerKey();
    String getUserId(String token);
    String getToken(String authorization);
}

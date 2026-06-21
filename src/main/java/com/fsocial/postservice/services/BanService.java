package com.fsocial.postservice.services;

public interface BanService {
    void ban(String token);
    void unBan(String token);
}

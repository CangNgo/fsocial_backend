package com.fsocial.services;

public interface BanService {
    void ban(String token);
    void unBan(String token);
}

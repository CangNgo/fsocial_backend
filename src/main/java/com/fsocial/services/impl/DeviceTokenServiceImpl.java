package com.fsocial.services.impl;

import com.fsocial.entity.DeviceToken;
import com.fsocial.repository.DeviceTokenRepository;
import com.fsocial.services.DeviceTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceTokenServiceImpl implements DeviceTokenService {

    DeviceTokenRepository deviceTokenRepository;

    @Override
    public void registerToken(String userId, String token, String deviceType) {

        Optional<DeviceToken> deviceToken = deviceTokenRepository.findByUserIdAndFcmToken(userId, token);
        if(deviceToken.isPresent()){
            DeviceToken dt = deviceToken.get();
            dt.setLastUsedAt(LocalDateTime.now());
            DeviceToken save = deviceTokenRepository.save(dt);

        }else {
            DeviceToken save = deviceTokenRepository.save(DeviceToken.builder()
                    .userId(userId)
                    .fcmToken(token)
                    .deviceType(deviceType)
                    .lastUsedAt(LocalDateTime.now())
                    .build());
        }

        return;
    }

    @Override
    public List<String> getTokenByUserId(String userId) {
        return deviceTokenRepository.findByUserId(userId).stream()
                .map(DeviceToken::getFcmToken)
                .toList();
    }

    @Override
    public void removeInvalidToken(String token) {
        deviceTokenRepository.deleteByFcmToken(token);
    }
}

package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.DeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(String userId);

    Optional<DeviceToken> findByUserIdAndDeviceId(String userId, String deviceId);

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByUserId(String userId);

    Optional<DeviceToken> findByUserIdAndFcmToken(String userId, String fcmToken);

    void deleteByFcmToken(String fcmToken);
}

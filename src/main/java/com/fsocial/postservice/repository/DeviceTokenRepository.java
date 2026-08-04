package com.fsocial.postservice.repository;

import com.fsocial.postservice.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, String> {

    List<DeviceToken> findByUserIdAndIsActiveTrue(String userId);

    Optional<DeviceToken> findByUserIdAndDeviceId(String userId, String deviceId);

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByUserId(String userId);

    Optional<DeviceToken> findByUserIdAndFcmToken(String userId, String fcmToken);

    @Modifying
    @Query("delete from DeviceToken d where d.fcmToken = :fcmToken")
    int deleteByFcmToken(@Param("fcmToken") String fcmToken);
}

package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.request.EmailRequest;
import com.fsocial.postservice.dto.request.OtpRequest;
import com.fsocial.postservice.dto.response.MailInformation;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.enums.RedisKeyType;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.services.OtpService;
import com.fsocial.postservice.util.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final MailUtils mailUtils;

    @NonFinal
    @Value("${otp.expiration.send}")
    long durationSend;

    @NonFinal
    @Value("${otp.expiration.verify}")
    long durationVerify;

    @Override
    public void sendOtp(String email, String keyPrefix) {
        String otp = generateOtp();
        String redisKey = keyPrefix + email;
        redisTemplate.opsForValue().set(redisKey, otp, durationSend, TimeUnit.MINUTES);
        //Tạo thông tin khi gửi email
        MailInformation mailInformation = MailInformation.builder()
                .to(email)
                .subject("Verification Code")
                .build();
        //Gửi email
        mailUtils.sendOtpResend(mailInformation, otp);
        log.info("OTP đã được gửi tới: {}", email);
    }

    @Override
    public void validateOtp(String email, String otp, String keyPrefix) {
        String redisKey = keyPrefix + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            log.warn("OTP không tồn tại hoặc đã hết hạn cho email: {}", email);
            throw new AppException(AccountErrorCode.OTP_INVALID);
        }
        if (!storedOtp.equals(otp)) {
            log.warn("OTP không hợp lệ cho email: {}", email);
            throw new AppException(AccountErrorCode.OTP_INVALID);
        }

        redisTemplate.opsForValue().set(redisKey, RedisKeyType.VALUE_AFTER_VERIFY.getType(), durationVerify, TimeUnit.SECONDS);
    }

    @Override
    public void deleteOtp(String email, String keyPrefix) {
        redisTemplate.delete(keyPrefix + email);
    }

    @Override
    public void validEmailBeforePersist(String email) {
        String redisKey = RedisKeyType.REGISTER.getRedisKeyPrefix() + email;
        String value = redisTemplate.opsForValue().get(redisKey);
        if (!RedisKeyType.VALUE_AFTER_VERIFY.getType().equals(value)) {
            log.warn("Email chưa được xác thực: {}", email);
            throw new AppException(AccountErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void sortTypeForSendOtp(EmailRequest request) {
        sendOtp(request.getEmail(), checkKeyPrefix(request.getType()));
    }

    @Override
    public void sortTypeForVerifyOtp(OtpRequest request) {
        validateOtp(request.getEmail(), request.getOtp(), checkKeyPrefix(request.getType()));
    }

    private String checkKeyPrefix(String type) {
        if (!type.equals(RedisKeyType.REGISTER.getType()) && !type.equals(RedisKeyType.RESET.getType())) {
            log.error("Sai loại yêu cầu: {}", type);
            throw new AppException(AccountErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return type.equals(RedisKeyType.REGISTER.getType())
                ? RedisKeyType.REGISTER.getRedisKeyPrefix()
                : RedisKeyType.RESET.getRedisKeyPrefix();
    }

    private String generateOtp() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}

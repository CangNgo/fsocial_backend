package com.fsocial.services;

import com.fsocial.dto.request.EmailRequest;
import com.fsocial.dto.request.OtpRequest;

public interface OtpService {
    void sendOtp(String email, String keyPrefix);
    void validateOtp(String email, String otp, String keyPrefix);
    void deleteOtp(String email, String keyPrefix);
    void validEmailBeforePersist(String email);
    void sortTypeForSendOtp(EmailRequest request);
    void sortTypeForVerifyOtp(OtpRequest request);
}

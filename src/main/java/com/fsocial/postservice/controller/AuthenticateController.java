package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.google.GoogleDTORequest;
import com.fsocial.postservice.dto.request.*;
import com.fsocial.postservice.dto.response.AuthenticationResponse;
import com.fsocial.postservice.dto.response.DuplicationResponse;
import com.fsocial.postservice.dto.response.IntrospectResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.enums.AccountResponseStatus;
import com.fsocial.postservice.exception.AccountCheckedException;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.AuthenticationService;
import com.fsocial.postservice.services.OtpService;
import com.fsocial.postservice.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticateController {

    AuthenticationService authenticationService;
    RefreshTokenService refreshTokenService;
    AccountService accountService;
    OtpService otpService;

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid TokenRequest token) {
        return ApiResponse.<IntrospectResponse>builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .data(authenticationService.introspect(token.getToken()))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> handleLogin(@RequestBody @Valid AccountLoginRequest request,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest httpRequest) throws AccountCheckedException {
        return ApiResponse.<AuthenticationResponse>builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .data(authenticationService.login(request, userAgent, httpRequest))
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshAccessToken(@RequestBody @Valid RefreshTokenRequest request,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest httpRequest) {
        return ApiResponse.<AuthenticationResponse>builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .data(refreshTokenService.refreshAccessToken(request.getRefreshToken(), userAgent, httpRequest.getRemoteAddr()))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody @Valid TokenRequest refreshToken, HttpServletRequest httpRequest) {
        refreshTokenService.disableRefreshToken(refreshToken.getToken());
        SecurityContextHolder.clearContext();
        httpRequest.getSession().invalidate();
        return ApiResponse.builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .build();
    }


    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        accountService.resetPassword(request.getEmail(), request.getNewPassword());
        return ApiResponse.<Void>builder()
                .statusCode(AccountResponseStatus.PASSWORD_RESET_SUCCESS.getCODE())
                .message(AccountResponseStatus.PASSWORD_RESET_SUCCESS.getMessage())
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<Account> persistAccount(@RequestBody @Valid AccountRegisterRequest request) {
        Account account = accountService.persistAccount(request);
        return ApiResponse.<Account>builder()
                .statusCode(AccountResponseStatus.ACCOUNT_REGISTERED.getCODE())
                .message(AccountResponseStatus.ACCOUNT_REGISTERED.getMessage())
                .data(account)
                .build();
    }
    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtp(@RequestBody @Valid EmailRequest request) {
        otpService.sortTypeForSendOtp(request);
        return ApiResponse.<Void>builder()
                .statusCode(AccountResponseStatus.OTP_SENT.getCODE())
                .message(AccountResponseStatus.OTP_SENT.getMessage())
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody @Valid OtpRequest request) {
        otpService.sortTypeForVerifyOtp(request);
        return ApiResponse.<Void>builder()
                .statusCode(AccountResponseStatus.OTP_VALID.getCODE())
                .message(AccountResponseStatus.OTP_VALID.getMessage())
                .build();
    }

    @PostMapping("/check-duplication")
    public ResponseEntity<ApiResponse<DuplicationResponse>> checkDuplication(@RequestBody @Valid DuplicationRequest request) {
        ApiResponse<DuplicationResponse> response = accountService.checkDuplication(request);
        HttpStatus status = response.getStatusCode() != 200 ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/google/login")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(
            @RequestBody @Valid GoogleDTORequest request,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest httpRequest
            ){

        return ApiResponse.<AuthenticationResponse>builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .data(authenticationService.loginWithGoogle(request, userAgent, httpRequest))
                .build();
    }
}

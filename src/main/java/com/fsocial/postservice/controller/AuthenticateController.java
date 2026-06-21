package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.request.AccountLoginRequest;
import com.fsocial.postservice.dto.request.RefreshTokenRequest;
import com.fsocial.postservice.dto.request.TokenRequest;
import com.fsocial.postservice.dto.response.AuthenticationResponse;
import com.fsocial.postservice.dto.response.IntrospectResponse;
import com.fsocial.postservice.enums.AccountResponseStatus;
import com.fsocial.postservice.exception.AccountCheckedException;
import com.fsocial.postservice.services.AuthenticationService;
import com.fsocial.postservice.services.JwtService;
import com.fsocial.postservice.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticateController {

    AuthenticationService authenticationService;
    RefreshTokenService refreshTokenService;
    JwtService jwtService;

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid TokenRequest token) {
        return buildResponse(authenticationService.introspect(token.getToken()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> handleLogin(@RequestBody @Valid AccountLoginRequest request,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest httpRequest) throws AccountCheckedException {
        return buildResponse(authenticationService.login(request, userAgent, httpRequest));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshAccessToken(@RequestBody @Valid RefreshTokenRequest request,
            @RequestHeader("User-Agent") String userAgent,
            HttpServletRequest httpRequest) {
        return buildResponse(refreshTokenService.refreshAccessToken(request.getRefreshToken(), userAgent, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody @Valid TokenRequest refreshToken, HttpServletRequest httpRequest) {
        refreshTokenService.disableRefreshToken(refreshToken.getToken());
        SecurityContextHolder.clearContext();
        httpRequest.getSession().invalidate();
        return buildResponse(null);
    }

    private <T> ApiResponse<T> buildResponse(T data) {
        return ApiResponse.<T>builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .dateTime(LocalDateTime.now())
                .data(data)
                .build();
    }
}

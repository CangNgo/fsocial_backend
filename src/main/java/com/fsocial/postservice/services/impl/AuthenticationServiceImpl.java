package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.request.AccountLoginRequest;
import com.fsocial.postservice.dto.response.AuthenticationResponse;
import com.fsocial.postservice.dto.response.IntrospectResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Token;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.exception.AccountCheckedException;
import com.fsocial.postservice.exception.AccountException;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.TokenRepository;
import com.fsocial.postservice.services.AuthenticationService;
import com.fsocial.postservice.services.JwtService;
import com.fsocial.postservice.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    RefreshTokenService refreshTokenService;
    TokenRepository tokenRepository;

    @Override
    public AuthenticationResponse login(AccountLoginRequest request, String userAgent, HttpServletRequest httpRequest) throws AccountCheckedException {
        Account account = accountRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .filter(acc -> acc.getPassword() != null && passwordEncoder.matches(request.getPassword(), acc.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Sai tên tài khoản hoặc mật khẩu: {}", request.getUsername());
                    return new AccountException(AccountErrorCode.LOGIN_FAILED);
                });

        if (!account.isStatus()) throw new AccountCheckedException(AccountErrorCode.ACCOUNT_BANNED);

        String ipAddress = httpRequest.getRemoteAddr();
        String accessToken = jwtService.generateToken(account.getUsername());

        Optional<Token> existingToken = tokenRepository.findByAccount(account);
        Token tokenEntity;
        if (existingToken.isPresent()) {
            tokenEntity = existingToken.get();
            tokenEntity.setToken(accessToken);
        } else {
            tokenEntity = Token.builder().token(accessToken).build();
        }
        tokenEntity.setAccount(account);
        tokenRepository.save(tokenEntity);

        String refreshToken = refreshTokenService.createRefreshToken(account.getUsername(), userAgent, ipAddress).getToken();

        log.info("Người dùng {} đăng nhập thành công từ IP: {}", request.getUsername(), ipAddress);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public IntrospectResponse introspect(String token) {
        boolean valid = jwtService.verifyToken(token);
        return IntrospectResponse.builder().valid(valid).build();
    }
}

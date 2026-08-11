package com.fsocial.services.impl;

import com.fsocial.dto.google.GoogleDTORequest;
import com.fsocial.dto.google.GoogleUserInfo;
import com.fsocial.dto.notification.NotificationDTO;
import com.fsocial.dto.request.AccountLoginRequest;
import com.fsocial.dto.response.AuthenticationResponse;
import com.fsocial.dto.response.IntrospectResponse;
import com.fsocial.entity.Account;
import com.fsocial.entity.AuthProviderCredential;
import com.fsocial.entity.Role;
import com.fsocial.entity.Token;
import com.fsocial.enums.AccountErrorCode;
import com.fsocial.enums.AuthProvider;
import com.fsocial.enums.NotificationType;
import com.fsocial.exception.AppException;
import com.fsocial.exception.StatusCode;
import com.fsocial.exception.*;
import com.fsocial.publisher.NotificationEvent;
import com.fsocial.repository.AccountRepository;
import com.fsocial.repository.AuthProviderRepository;
import com.fsocial.repository.RoleRepository;
import com.fsocial.repository.TokenRepository;
import com.fsocial.services.AuthenticationService;
import com.fsocial.services.GoogleOAuthService;
import com.fsocial.services.JwtService;
import com.fsocial.services.RefreshTokenService;
import com.fsocial.util.DefaultMediaProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    AccountRepository accountRepository;
    AuthProviderRepository authProviderRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    RefreshTokenService refreshTokenService;
    TokenRepository tokenRepository;
    GoogleOAuthService googleOAuthService;
    RoleRepository roleRepository;
    DefaultMediaProvider defaultMediaProvider;
    NotificationEvent notificationEvent;

    @Override
    public AuthenticationResponse login(AccountLoginRequest request, String userAgent, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Sai tên tài khoản hoặc mật khẩu: {}", request.getUsername());
                    return new AppException(AccountErrorCode.LOGIN_FAILED);
                });

        authProviderRepository.findByAccount_IdAndProvider(account.getId(), AuthProvider.LOCAL)
                .filter(auth -> auth.getPassword() != null && passwordEncoder.matches(request.getPassword(), auth.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Sai tên tài khoản hoặc mật khẩu: {}", request.getUsername());
                    return new AppException(AccountErrorCode.LOGIN_FAILED);
                });

        if (!account.isStatus()) throw new AppException(AccountErrorCode.ACCOUNT_BANNED);

       return saveToken(account, userAgent, httpRequest);
    }

    @Override
    public IntrospectResponse introspect(String token) {
        boolean valid = jwtService.verifyToken(token);
        return IntrospectResponse.builder().valid(valid).build();
    }

    public AuthenticationResponse saveToken(Account account, String userAgent, HttpServletRequest httpRequest){

        String ipAddress = httpRequest.getRemoteAddr();
        String accessToken = jwtService.generateToken(account.getId());

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

        String refreshToken = refreshTokenService.createRefreshToken(account.getId(), userAgent, ipAddress).getToken();

        notificationEvent.publishCreateNotification(new NotificationDTO(
                account.getId(),
                account.getId(),
                NotificationType.LOGIN
        ));

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthenticationResponse loginWithGoogle(GoogleDTORequest request,  String userAgent, HttpServletRequest httpRequest){
        GoogleIdToken.Payload payload = googleOAuthService.verify(request.code());

        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("name"),
                (String) payload.get("given_name"),
                (String) payload.get("family_name"),
                (String) payload.get("picture"),
                (String) payload.get("locale")
        );
        log.info("Google login for email: {}", googleUserInfo.email());

        // 1: đã từng login Google trước đó -> tra theo providerUserId
        Optional<AuthProviderCredential> linkedAuth = authProviderRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, googleUserInfo.googleId());
        if (linkedAuth.isPresent()) {
            Account existingAccount = linkedAuth.get().getAccount();
            if (!existingAccount.isStatus()) throw new AppException(AccountErrorCode.ACCOUNT_BANNED);
            return this.saveToken(existingAccount, userAgent, httpRequest);
        }

        // 2: account đã tồn tại (vd đăng ký local cùng email) -> link thêm Google vào account đó
        Optional<Account> accountByEmail = accountRepository.findByEmail(googleUserInfo.email());
        if (accountByEmail.isPresent()) {
            Account existingAccount = accountByEmail.get();
            if (!existingAccount.isStatus()) throw new AppException(AccountErrorCode.ACCOUNT_BANNED);

            authProviderRepository.save(AuthProviderCredential.builder()
                    .account(existingAccount)
                    .provider(AuthProvider.GOOGLE)
                    .providerUserId(googleUserInfo.googleId())
                    .build());

            return this.saveToken(existingAccount, userAgent, httpRequest);
        }

        // 3: chưa có gì -> tạo account mới (username/password null) + auth_provider GOOGLE
        Role role = roleRepository.findByName("USER").orElseThrow(() -> new AppException(StatusCode.ROLE_NOT_FOUND));

        String seed = googleUserInfo.email();
        Account accountRegister = accountRepository.save(Account.builder()
                        .email(googleUserInfo.email())
                        .firstName(googleUserInfo.givenName())
                        .lastName(googleUserInfo.familyName())
                        .displayName(googleUserInfo.displayName())
                        .role(role)
                        .avatar(googleUserInfo.picture())
                        .background(defaultMediaProvider.pickBackground(seed))
                        .address(googleUserInfo.locale())
                        .status(true)
                .build());

        authProviderRepository.save(AuthProviderCredential.builder()
                .account(accountRegister)
                .provider(AuthProvider.GOOGLE)
                .providerUserId(googleUserInfo.googleId())
                .build());

        return this.saveToken(accountRegister, userAgent, httpRequest);
    }

}

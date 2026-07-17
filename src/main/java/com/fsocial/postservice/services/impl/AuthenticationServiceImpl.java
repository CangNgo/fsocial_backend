package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.google.GoogleDTORequest;
import com.fsocial.postservice.dto.google.GoogleUserInfo;
import com.fsocial.postservice.dto.notification.NotificationDTO;
import com.fsocial.postservice.dto.request.AccountLoginRequest;
import com.fsocial.postservice.dto.request.AccountRegisterRequest;
import com.fsocial.postservice.dto.response.AuthenticationResponse;
import com.fsocial.postservice.dto.response.IntrospectResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Role;
import com.fsocial.postservice.entity.Token;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.enums.AuthProvider;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.exception.*;
import com.fsocial.postservice.publisher.NotificationEvent;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.RoleRepository;
import com.fsocial.postservice.repository.TokenRepository;
import com.fsocial.postservice.services.*;
import com.fsocial.postservice.util.DefaultMediaProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    GoogleOAuthService googleOAuthService;
    RoleRepository roleRepository;
    DefaultMediaProvider defaultMediaProvider;
    NotificationEvent notificationEvent;

    @Override
    public AuthenticationResponse login(AccountLoginRequest request, String userAgent, HttpServletRequest httpRequest) {
        Account account = accountRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .filter(acc -> acc.getPassword() != null && passwordEncoder.matches(request.getPassword(), acc.getPassword()))
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

        //5: find or create
        Optional<Account> account = accountRepository.findByEmail(googleUserInfo.email()) ;

        if(account.isEmpty()){

            Role role = roleRepository.findByName("USER").orElseThrow(() -> new AppException(StatusCode.ROLE_NOT_FOUND));

            String seed = googleUserInfo.email();
            Account accountRegister = accountRepository.save(Account.builder()
                            .username(googleUserInfo.email())
                            .email(googleUserInfo.email())
                            .firstName(googleUserInfo.givenName())
                            .lastName(googleUserInfo.familyName())
                            .displayName(googleUserInfo.displayName())
                            .role(role)
                            .provider(AuthProvider.GOOGLE)
                            .avatar(googleUserInfo.picture())
                            .background(defaultMediaProvider.pickBackground(seed))
                            .address(googleUserInfo.locale())
                            .status(true)
                            .googleId(googleUserInfo.googleId())
                    .build());

            return this.saveToken(accountRegister, userAgent, httpRequest);
        }else {
            Account existingAccount = account.get();
            if (!existingAccount.isStatus()) throw new AppException(AccountErrorCode.ACCOUNT_BANNED);
            return this.saveToken(existingAccount, userAgent, httpRequest);
        }
    }

}

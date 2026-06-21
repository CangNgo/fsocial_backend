package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.exception.AccountException;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.services.JwtService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jodd.exception.UncheckedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JwtServiceImpl implements JwtService {

    AccountRepository accountRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;

    @NonFinal
    @Value("${jwt.duration}")
    long durationTime;

    @Override
    public String generateToken(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AccountException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        JWTClaimsSet claimsSet = buildClaimsSet(account);
        return signToken(claimsSet);
    }

    @Override
    public boolean verifyToken(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(getSignerKey());
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.verify(verifier) && !isTokenExpired(signedJWT);
        } catch (JOSEException | ParseException e) {
            log.error("Có lỗi trong quá trình phân tích Token.");
            throw new AccountException(AccountErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public byte[] getSignerKey() {
        if (signerKey == null || signerKey.isEmpty()) throw new AccountException(AccountErrorCode.UNCATEGORIZED_EXCEPTION);
        return signerKey.getBytes();
    }

    @Override
    public String getUserId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("Không thể lấy userId từ token: {}", e.getMessage());
            throw new AccountException(AccountErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public String getToken(String authorization) {
        if(authorization == null || !authorization.startsWith("Bearer ")){
            throw new UncheckedException("Authorization not found");
        }
        return authorization.substring(7);
    }

    private JWTClaimsSet buildClaimsSet(Account account) {
        String scope = account.getRole() != null ? account.getRole().getName() : "";
        log.info("[JWT-DEBUG] accountId={}, username={}, roleObj={}, roleName={}, scopeClaim='{}'",
                account.getId(),
                account.getUsername(),
                account.getRole(),
                account.getRole() != null ? account.getRole().getName() : "NULL",
                scope);
        return new JWTClaimsSet.Builder()
                .subject(account.getId())
                .issuer("FSOCIAL - FCODER")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(durationTime, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .build();
    }

    private String signToken(JWTClaimsSet claimsSet) {
        try {
            JWSObject jwsObject = new JWSObject(
                    new JWSHeader(JWSAlgorithm.HS256),
                    new Payload(claimsSet.toJSONObject())
            );
            byte[] key = getSignerKey();
            if (key.length < 32) throw new AccountException(AccountErrorCode.WEAK_SECRET_KEY);
            jwsObject.sign(new MACSigner(key));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Không tạo được token: {}", e.getMessage(), e);
            throw new AccountException(AccountErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private boolean isTokenExpired(SignedJWT signedJWT) throws ParseException {
        return signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().isBefore(Instant.now());
    }
}

package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.request.AccountRegisterRequest;
import com.fsocial.postservice.dto.request.DuplicationRequest;
import com.fsocial.postservice.dto.response.*;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.RefreshToken;
import com.fsocial.postservice.entity.Token;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.enums.AccountResponseStatus;
import com.fsocial.postservice.enums.RedisKeyType;
import com.fsocial.postservice.exception.AccountException;
import com.fsocial.postservice.exception.AppUnCheckedException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.mapper.AccountMapper;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.RefreshTokenRepository;
import com.fsocial.postservice.repository.RoleRepository;
import com.fsocial.postservice.repository.TokenRepository;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.BanService;
import com.fsocial.postservice.services.OtpService;
import com.fsocial.postservice.util.DefaultMediaProvider;
import com.fsocial.postservice.util.DisplayNameUtils;
import jodd.exception.UncheckedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    AccountRepository accountRepository;
    RoleRepository roleRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    OtpService otpService;
    BanService banService;
    TokenRepository tokenRepository;
    RefreshTokenRepository refreshTokenRepository;
    DefaultMediaProvider defaultMediaProvider;

    static String DEFAULT_ROLE = "USER";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Account persistAccount(AccountRegisterRequest request) {
        Account account = saveAccount(request);
//        createProfile(account, request);
        otpService.deleteOtp(request.getEmail(), RedisKeyType.REGISTER.getRedisKeyPrefix());
        return account;
    }

    @Override
    public AccountResponse getUser(String id) {
        return accountRepository.findById(id)
                .map((acc) -> AccountResponse.builder()
                        .id(acc.getId())
                        .username(acc.getUsername())
                        .displayName(DisplayNameUtils.build(acc))
                        .avatar(acc.getAvatar())
                        .background(acc.getBackground())
                        .isKOL(acc.isKOL())
                        .role(acc.getRole().getName())
                        .bio(acc.getBio())
                        .build())
                .orElseThrow(() -> new AccountException(AccountErrorCode.ACCOUNT_NOT_EXISTED));
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        account.setPassword(passwordEncoder.encode(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Đặt lại mật khẩu thành công.");
    }

    @Override
    public ApiResponse<DuplicationResponse> checkDuplication(DuplicationRequest request) {
        boolean usernameExisted = accountRepository.countByUsername(request.getUsername()) > 0;
        boolean emailExisted = accountRepository.countByEmail(request.getEmail()) > 0;

        DuplicationResponse response = DuplicationResponse.builder()
                .username(usernameExisted ? AccountErrorCode.USERNAME_EXISTED.getMessage() : null)
                .email(emailExisted ? AccountErrorCode.EMAIL_EXISTED.getMessage() : null)
                .build();

        boolean hasError = usernameExisted || emailExisted;

        return ApiResponse.<DuplicationResponse>builder()
                .statusCode(hasError ? AccountErrorCode.DUPLICATION.getCode() : AccountResponseStatus.VALID.getCODE())
                .message(hasError ? AccountErrorCode.DUPLICATION.getMessage() : AccountResponseStatus.VALID.getMessage())
                .data(hasError ? response : null)
                .build();
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AccountException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new AccountException(AccountErrorCode.WRONG_PASSWORD);
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Đổi mật khẩu thành công.");
    }

    @Override
    public boolean existsById(String id) {
        return accountRepository.findById(id).isPresent();
    }

    @Override
    public List<AccountStatisticRegisterDTO> countByCreatedAtByHours(LocalDateTime startDay, LocalDateTime endDay) {
        List<AccountRepository.HourCountResult> results = accountRepository.countByCreatedAtByHours(startDay, endDay);

        Map<Integer, Integer> map = new HashMap<>();
        for (AccountRepository.HourCountResult row : results) {
            if (row._id() != null) map.put(row._id(), row.count() != null ? row.count() : 0);
        }

        List<AccountStatisticRegisterDTO> res = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            res.add(new AccountStatisticRegisterDTO(String.format("%02d:00", hour), map.getOrDefault(hour, 0)));
        }
        return res;
    }

    @Override
    public List<AccountStatisticRegisterLongDateDTO> countByCreatedAtByStartEnd(LocalDateTime startDay, LocalDateTime endDay) {
        List<AccountRepository.DateCountResult> results = accountRepository.countByCreatedAtByDate(startDay, endDay);

        Map<String, Long> dateCountMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (AccountRepository.DateCountResult row : results) {
            if (row._id() != null) dateCountMap.put(row._id(), row.count() != null ? row.count() : 0L);
        }

        List<AccountStatisticRegisterLongDateDTO> res = new ArrayList<>();
        LocalDate start = startDay.toLocalDate();
        LocalDate end = endDay.toLocalDate();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            String dateStr = current.format(formatter);
            Date date = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());
            res.add(new AccountStatisticRegisterLongDateDTO(date, dateCountMap.getOrDefault(dateStr, 0L)));
            current = current.plusDays(1);
        }
        return res;
    }

    @Override
    @Transactional
    public String banUser(String userId) {
        Account banAccount = accountRepository.findById(userId)
                .orElseThrow(() -> new AccountException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        Optional<Token> tokenAccount = tokenRepository.findByAccount(banAccount);
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findFirstByUsernameOrderByExpiryDateDesc(banAccount.getUsername());

        banAccount.setStatus(false);
        accountRepository.save(banAccount);

        tokenAccount.ifPresent(token -> banService.ban(token.getToken()));
        refreshToken.ifPresent(refresh -> refreshTokenRepository.deleteByToken(refresh.getToken()));

        return "Ban account: " + banAccount.getUsername() + " successful";
    }

    @Override
    public AccountResponse getProfile(String userId) {

        Optional<Account> account = accountRepository.findById(userId);
        if (account.isPresent()) {
            Account acc = account.get();
            return AccountResponse.builder()
                    .id(acc.getId())
                    .username(acc.getUsername())
                    .displayName(DisplayNameUtils.build(acc))
                    .avatar(acc.getAvatar())
                    .background(acc.getBackground())
                    .isKOL(acc.isKOL())
                    .role(acc.getRole().getName())
                    .bio(acc.getBio())
                    .build();
        }
        throw new UncheckedException(AccountErrorCode.NOT_FOUND.getMessage());
    }

    @Override
    public ActorSnapshotDTO getOwner(String userId) {
        Optional<ActorSnapshotDTO> owner = accountRepository.findOwnerById(userId);
        if (owner.isEmpty()) {
            throw new AppUnCheckedException(StatusCode.USER_NOT_FOUND);
        }
        return owner.get();
    }

    private Account saveAccount(AccountRegisterRequest request) {
        Account account = accountMapper.toEntity(request);
        account.setCreatedAt(LocalDateTime.now());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new AccountException(AccountErrorCode.ROLE_NOT_FOUND)));
        account.setStatus(true);

        String seed = request.getUsername() != null ? request.getUsername() : request.getEmail();
        account.setAvatar(defaultMediaProvider.pickAvatar(seed));
        account.setBackground(defaultMediaProvider.pickBackground(seed));

        return accountRepository.save(account);
    }

}

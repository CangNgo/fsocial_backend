package com.fsocial.services.impl;

import com.fsocial.dto.ActorSnapshotDTO;
import com.fsocial.dto.ApiResponse;
import com.fsocial.dto.request.AccountRegisterRequest;
import com.fsocial.dto.request.DuplicationRequest;
import com.fsocial.dto.response.AccountResponse;
import com.fsocial.dto.response.AccountStatisticRegisterDTO;
import com.fsocial.dto.response.AccountStatisticRegisterLongDateDTO;
import com.fsocial.dto.response.DuplicationResponse;
import com.fsocial.dto.response.ManageUserResponse;
import com.fsocial.dto.response.SearchPageResponse;
import com.fsocial.entity.Account;
import com.fsocial.entity.AuthProviderCredential;
import com.fsocial.entity.RefreshToken;
import com.fsocial.entity.Token;
import com.fsocial.enums.AccountErrorCode;
import com.fsocial.enums.AccountResponseStatus;
import com.fsocial.enums.AuthProvider;
import com.fsocial.enums.RedisKeyType;
import com.fsocial.exception.AppException;
import com.fsocial.exception.StatusCode;
import com.fsocial.mapper.AccountMapper;
import com.fsocial.entity.Follow;
import com.fsocial.repository.AccountRepository;
import com.fsocial.repository.AuthProviderRepository;
import com.fsocial.repository.FollowRepository;
import com.fsocial.repository.RefreshTokenRepository;
import com.fsocial.repository.RoleRepository;
import com.fsocial.repository.TokenRepository;
import com.fsocial.services.AccountService;
import com.fsocial.services.BanService;
import com.fsocial.services.OtpService;
import com.fsocial.util.DefaultMediaProvider;
import com.fsocial.util.DisplayNameUtils;
import jodd.exception.UncheckedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    AccountRepository accountRepository;
    AuthProviderRepository authProviderRepository;
    FollowRepository followRepository;
    RoleRepository roleRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    OtpService otpService;
    BanService banService;
    TokenRepository tokenRepository;
    RefreshTokenRepository refreshTokenRepository;
    DefaultMediaProvider defaultMediaProvider;
    RedisTemplate<String, String> redisTemplate;

    static String DEFAULT_ROLE = "USER";
    private static final int TTL_SECONDS = 600; // time live of Redis in Seconds

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountResponse persistAccount(AccountRegisterRequest request) {
        Account account = saveAccount(request);
//        createProfile(account, request);
        otpService.deleteOtp(request.getEmail(), RedisKeyType.REGISTER.getRedisKeyPrefix());
        return toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getUser(String id) {
        return accountRepository.findById(id)
                .map(this::toAccountResponse)
                .orElseThrow(() -> new AppException(AccountErrorCode.ACCOUNT_NOT_EXISTED));
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        AuthProviderCredential localAuth = authProviderRepository
                .findByAccount_IdAndProvider(account.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> new AppException(AccountErrorCode.LOCAL_AUTH_NOT_FOUND));

        localAuth.setPassword(passwordEncoder.encode(newPassword));
        authProviderRepository.save(localAuth);
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
                .orElseThrow(() -> new AppException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        AuthProviderCredential localAuth = authProviderRepository
                .findByAccount_IdAndProvider(account.getId(), AuthProvider.LOCAL)
                .orElseThrow(() -> new AppException(AccountErrorCode.LOCAL_AUTH_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, localAuth.getPassword())) {
            throw new AppException(AccountErrorCode.WRONG_PASSWORD);
        }

        localAuth.setPassword(passwordEncoder.encode(newPassword));
        authProviderRepository.save(localAuth);
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
                .orElseThrow(() -> new AppException(AccountErrorCode.ACCOUNT_NOT_EXISTED));

        Optional<Token> tokenAccount = tokenRepository.findByAccount(banAccount);
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findFirstByAccountIdOrderByExpiryDateDesc(banAccount.getId());

        banAccount.setStatus(false);
        accountRepository.save(banAccount);

        tokenAccount.ifPresent(token -> banService.ban(token.getToken()));
        refreshToken.ifPresent(refresh -> refreshTokenRepository.deleteByToken(refresh.getToken()));

        return "Ban account: " + banAccount.getUsername() + " successful";
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getProfile(String userId) {

        Optional<Account> account = accountRepository.findById(userId);
        if (account.isPresent()) {
            return toAccountResponse(account.get());
        }
        throw new UncheckedException(AccountErrorCode.NOT_FOUND.getMessage());
    }

    @Override
    public ActorSnapshotDTO getOwner(String userId) {
        Optional<ActorSnapshotDTO> owner = accountRepository.findOwnerById(userId);
        if (owner.isEmpty()) {
            throw new AppException(StatusCode.USER_NOT_FOUND);
        }
        return owner.get();
    }

    @Override
    @Transactional
    public void follow(String userId, String targetId) {
        updateFollowRelation(userId, targetId, true);
        log.info("User {} followed user {}", userId, targetId);
        cacheFollowing(userId, targetId);
    }

    @Override
    public void unfollow(String userId, String targetId) {
        updateFollowRelation(userId, targetId, false);
        log.info("User {} unfollowed user {}", userId, targetId);
        removeCacheFollowing(userId, targetId);
    }

    @Override
    public boolean isFollowing(String userId, String targetId) {
        String followingKey = "following:" + targetId;
        Boolean isMember = redisTemplate.opsForSet().isMember(followingKey, userId);
        return isMember != null && isMember || getFollowers(targetId).contains(userId);
    }

    @Override
    public Set<String> getFollowers(String userId) {
        return getCachedFollowData("follower:" + userId, userId, true);
    }

    @Override
    public Set<String> getFollowing(String userId) {
        return getCachedFollowData("following:" + userId, userId, false);
    }

    @Override
    public List<ManageUserResponse> getAllUsers() {
        return accountRepository.findAll().stream()
                .map(account -> ManageUserResponse.builder()
                        .id(account.getId())
                        .username(account.getUsername())
                        .displayName(DisplayNameUtils.build(account))
                        .email(account.getEmail())
                        .createdAt(account.getCreatedAt())
                        .updatedAt(account.getUpdatedAt())
                        .status(account.isStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchPageResponse<AccountResponse> searchUsers(String keyword, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        String trimmedKeyword = keyword == null ? "" : keyword.trim();

        if (trimmedKeyword.isBlank()) {
            return new SearchPageResponse<>(List.of(), safePage, safeSize, false);
        }

        List<Account> accounts = accountRepository.searchByKeyword(
                trimmedKeyword,
                PageRequest.of(safePage, safeSize + 1)
        );

        boolean hasMore = accounts.size() > safeSize;
        List<AccountResponse> items = accounts.stream()
                .limit(safeSize)
                .map(this::toAccountResponse)
                .toList();

        return new SearchPageResponse<>(items, safePage, safeSize, hasMore);
    }

    /** 1 dòng trong bảng follow thay cho việc mutate 2 Set rồi save 2 document như bản Mongo. */
    private void updateFollowRelation(String userId, String targetId, boolean isFollow) {
        if (!accountRepository.existsById(targetId) || !accountRepository.existsById(userId))
            throw new AppException(AccountErrorCode.ACCOUNT_NOT_EXISTED);

        if (isFollow) {
            // PK (follower_id, followee_id) đảm bảo idempotent
            if (!followRepository.existsByFollowerIdAndFolloweeId(userId, targetId))
                followRepository.save(new Follow(userId, targetId));
        } else {
            followRepository.deleteByFollowerIdAndFolloweeId(userId, targetId);
        }
    }

    private Set<String> getCachedFollowData(String cacheKey, String targetId, boolean isFollower) {
        Set<String> data = redisTemplate.opsForSet().members(cacheKey);
        if (data != null && !data.isEmpty()) return data;

        data = fetchFollowDataFromDB(targetId, isFollower);
        if (!data.isEmpty()) updateRedis(cacheKey, data);
        return data;
    }

    private Set<String> fetchFollowDataFromDB(String targetId, boolean isFollower) {
        if (!accountRepository.existsById(targetId))
            throw new AppException(StatusCode.USER_NOT_FOUND);
        return new HashSet<>(isFollower
                ? followRepository.findFollowerIds(targetId)
                : followRepository.findFolloweeIds(targetId));
    }

    private void updateRedis(String key, Set<String> data) {
        redisTemplate.opsForSet().add(key, data.toArray(new String[0]));
        redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void cacheFollowing(String followerId, String targetId) {
        String key = "following:" + followerId;
        redisTemplate.opsForSet().add(key, targetId);
        redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);
    }

    private void removeCacheFollowing(String followerId, String targetId) {
        redisTemplate.opsForSet().remove("following:" + followerId, targetId);
    }

    private Account saveAccount(AccountRegisterRequest request) {
        Account account = accountMapper.toEntity(request);
        account.setCreatedAt(LocalDateTime.now());
        account.setRole(roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new AppException(AccountErrorCode.ROLE_NOT_FOUND)));
        account.setStatus(true);

        String seed = request.getUsername() != null ? request.getUsername() : request.getEmail();
        account.setAvatar(defaultMediaProvider.pickAvatar(seed));
        account.setBackground(defaultMediaProvider.pickBackground(seed));

        Account savedAccount = accountRepository.save(account);

        authProviderRepository.save(AuthProviderCredential.builder()
                .account(savedAccount)
                .provider(AuthProvider.LOCAL)
                .password(passwordEncoder.encode(request.getPassword()))
                .build());

        return savedAccount;
    }

    private AccountResponse toAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .dob(account.getDob() != null ? account.getDob().toString() : null)
                .gender(account.getGender())
                .address(account.getAddress())
                .displayName(DisplayNameUtils.build(account))
                .avatar(account.getAvatar())
                .background(account.getBackground())
                .isKOL(account.isKOL())
                .role(account.getRole().getName())
                .bio(account.getBio())
                .follower(new HashSet<>(followRepository.findFollowerIds(account.getId())))
                .following(new HashSet<>(followRepository.findFolloweeIds(account.getId())))
                .build();
    }

}

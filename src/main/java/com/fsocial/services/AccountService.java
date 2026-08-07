package com.fsocial.services;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AccountService {
    AccountResponse persistAccount(AccountRegisterRequest request);
    AccountResponse getUser(String id);
    void resetPassword(String email, String newPassword);
    ApiResponse<DuplicationResponse> checkDuplication(DuplicationRequest request);
    void changePassword(String userId, String oldPassword, String newPassword);
    boolean existsById(String id);
    List<AccountStatisticRegisterDTO> countByCreatedAtByHours(LocalDateTime startDay, LocalDateTime endDay);
    List<AccountStatisticRegisterLongDateDTO> countByCreatedAtByStartEnd(LocalDateTime startDay, LocalDateTime endDay);
    String banUser(String userId);
    AccountResponse getProfile (String userId);
    ActorSnapshotDTO getOwner(String userId);
    void follow(String userId, String targetId);
    void unfollow(String userId, String targetId);
    boolean isFollowing(String userId, String targetId);
    Set<String> getFollowers(String userId);
    Set<String> getFollowing(String userId);
    List<ManageUserResponse> getAllUsers();
    SearchPageResponse<AccountResponse> searchUsers(String keyword, int page, int size);
}

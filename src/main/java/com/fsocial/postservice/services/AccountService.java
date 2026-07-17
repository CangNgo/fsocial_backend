package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.request.AccountRegisterRequest;
import com.fsocial.postservice.dto.request.DuplicationRequest;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.dto.response.AccountStatisticRegisterDTO;
import com.fsocial.postservice.dto.response.AccountStatisticRegisterLongDateDTO;
import com.fsocial.postservice.dto.response.DuplicationResponse;
import com.fsocial.postservice.dto.response.ManageUserResponse;
import com.fsocial.postservice.dto.response.SearchPageResponse;
import com.fsocial.postservice.entity.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AccountService {
    Account persistAccount(AccountRegisterRequest request);
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

package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.Account.OwnerDTO;
import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.request.AccountRegisterRequest;
import com.fsocial.postservice.dto.request.DuplicationRequest;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.dto.response.AccountStatisticRegisterDTO;
import com.fsocial.postservice.dto.response.AccountStatisticRegisterLongDateDTO;
import com.fsocial.postservice.dto.response.DuplicationResponse;
import com.fsocial.postservice.entity.Owner;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountService {
    void persistAccount(AccountRegisterRequest request);
    AccountResponse getUser(String id);
    void resetPassword(String email, String newPassword);
    ApiResponse<DuplicationResponse> checkDuplication(DuplicationRequest request);
    void changePassword(String userId, String oldPassword, String newPassword);
    boolean existsById(String id);
    List<AccountStatisticRegisterDTO> countByCreatedAtByHours(LocalDateTime startDay, LocalDateTime endDay);
    List<AccountStatisticRegisterLongDateDTO> countByCreatedAtByStartEnd(LocalDateTime startDay, LocalDateTime endDay);
    String banUser(String userId);
    AccountResponse getProfile (String userId);
    OwnerDTO getOwner(String userId);
}

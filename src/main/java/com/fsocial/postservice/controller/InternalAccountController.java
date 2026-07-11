package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.services.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/internal/account")
public class InternalAccountController {

    AccountService accountService;

    @GetMapping("/valid-id/{userId}")
    public ApiResponse<Boolean> validUserId(@PathVariable String userId) {
        return ApiResponse.<Boolean>builder()
                .data(accountService.existsById(userId))
                .message("Kiểm tra userId thành công")
                .build();
    }
}

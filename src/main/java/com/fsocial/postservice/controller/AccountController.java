package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.relationships.FollowRequest;
import com.fsocial.postservice.dto.request.ChangePasswordRequest;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.dto.response.AccountStatisticRegisterDTO;
import com.fsocial.postservice.dto.response.AccountStatisticRegisterLongDateDTO;
import com.fsocial.postservice.dto.response.ManageUserResponse;
import com.fsocial.postservice.dto.response.SearchPageResponse;
import com.fsocial.postservice.enums.AccountResponseStatus;
import com.fsocial.postservice.enums.ResponseStatus;
import com.fsocial.postservice.services.AccountService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {

    AccountService accountService;

    @GetMapping("/{userId}")
    public ApiResponse<AccountResponse> getAccount(@PathVariable String userId) {
        return ApiResponse.<AccountResponse>builder()
                .statusCode(AccountResponseStatus.SUCCESS.getCODE())
                .message(AccountResponseStatus.SUCCESS.getMessage())
                .data(accountService.getUser(userId))
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        accountService.changePassword(jwt.getSubject(), request.getOldPassword(), request.getNewPassword());
        return ApiResponse.<Void>builder()
                .statusCode(AccountResponseStatus.PASSWORD_CHANGED.getCODE())
                .message(AccountResponseStatus.PASSWORD_CHANGED.getMessage())
                .build();
    }

    @GetMapping("/exists")
    public ApiResponse<Map<String, Boolean>> existsAccountByUserId(@RequestParam String userId) {
        Map<String, Boolean> exists = new HashMap<>();
        exists.put("exists", accountService.existsById(userId));
        return ApiResponse.<Map<String, Boolean>>builder()
                .data(exists)
                .message("Kiểm tra userId có tồn tại hay không thành công")
                .build();
    }

    @GetMapping("/statistics_register_today")
    public ApiResponse<List<AccountStatisticRegisterDTO>> statisticsRegister(@RequestParam("date_time") String dateTime) {
        LocalDate date = LocalDate.parse(dateTime);
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);
        List<AccountStatisticRegisterDTO> res = accountService.countByCreatedAtByHours(startDate, endDate);
        return ApiResponse.<List<AccountStatisticRegisterDTO>>builder()
                .data(res)
                .message("Thống kê số lượng tài khoản được tạo trong ngày " + date + " thành công")
                .build();
    }

    @GetMapping("/statistics_register_start_end")
    public ApiResponse<List<AccountStatisticRegisterLongDateDTO>> statisticsRegisterStartEnd(
            @RequestParam("startDate") String startDateRe, @RequestParam("endDate") String endDateRe) {
        LocalDate start = LocalDate.parse(startDateRe);
        LocalDate end = LocalDate.parse(endDateRe);
        List<AccountStatisticRegisterLongDateDTO> res = accountService.countByCreatedAtByStartEnd(
                start.atStartOfDay(), end.atTime(23, 59, 59));
        return ApiResponse.<List<AccountStatisticRegisterLongDateDTO>>builder()
                .data(res)
                .message("Lấy danh sách thống kê từ " + start + " đến " + end + " thành công")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<ManageUserResponse>> getAllUsers() {
        return ApiResponse.<List<ManageUserResponse>>builder()
                .data(accountService.getAllUsers())
                .message("Lấy danh sách người dùng thành công")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ban")
    public ApiResponse<Object> banAccount(@RequestParam("user_id") String userId) {
        return ApiResponse.<Object>builder()
                .data(accountService.banUser(userId))
                .message("Ban tài khoản thành công")
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<AccountResponse> getProfile(@AuthenticationPrincipal Jwt jwt){

        return ApiResponse.<AccountResponse>builder()
                .data(accountService.getProfile(jwt.getSubject()))
                .message("Lấy thông tin tài khoản thành công")
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<SearchPageResponse<AccountResponse>> searchUsers(
            @RequestParam("q") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.<SearchPageResponse<AccountResponse>>builder()
                .data(accountService.searchUsers(keyword, page, size))
                .message("Tìm kiếm người dùng thành công")
                .build();
    }

    @PostMapping("/follow")
    public ApiResponse<Void> follow(@RequestBody @Valid FollowRequest request,
                                    @AuthenticationPrincipal Jwt jwt
                                    ) {
        accountService.follow(jwt.getSubject(), request.getFollowing());
        return ApiResponse.<Void>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    @PostMapping("/unfollow")
    public ApiResponse<Void> unfollow(@RequestBody @Valid FollowRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        accountService.unfollow(jwt.getSubject(), request.getFollowing());
        return ApiResponse.<Void>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    @GetMapping("/followers/{userId}")
    public ApiResponse<Set<String>> getFollowers(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<Set<String>>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .data(accountService.getFollowers(jwt.getSubject()))
                .build();
    }

    @GetMapping("/followings/{userId}")
    public ApiResponse<Set<String>> getFollowings(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.<Set<String>>builder()
                .statusCode(ResponseStatus.SUCCESS.getCODE())
                .message(ResponseStatus.SUCCESS.getMessage())
                .data(accountService.getFollowing(jwt.getSubject()))
                .build();
    }
}
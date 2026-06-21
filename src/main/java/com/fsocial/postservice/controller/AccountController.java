package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.profile.ProfileDTO;
import com.fsocial.postservice.dto.request.*;
import com.fsocial.postservice.dto.response.*;
import com.fsocial.postservice.enums.AccountResponseStatus;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.JwtService;
import com.fsocial.postservice.services.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jodd.exception.UncheckedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {

    AccountService accountService;
    OtpService otpService;
    JwtService jwtService;
    HttpServletRequest httpServletRequest;

    @PostMapping("/register")
    public ApiResponse<Void> persistAccount(@RequestBody @Valid AccountRegisterRequest request) {
        accountService.persistAccount(request);
        return buildResponse(null, AccountResponseStatus.ACCOUNT_REGISTERED);
    }

    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtp(@RequestBody @Valid EmailRequest request) {
        otpService.sortTypeForSendOtp(request);
        return buildResponse(null, AccountResponseStatus.OTP_SENT);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody @Valid OtpRequest request) {
        otpService.sortTypeForVerifyOtp(request);
        return buildResponse(null, AccountResponseStatus.OTP_VALID);
    }

    @PostMapping("/check-duplication")
    public ResponseEntity<ApiResponse<DuplicationResponse>> checkDuplication(@RequestBody @Valid DuplicationRequest request) {
        ApiResponse<DuplicationResponse> response = accountService.checkDuplication(request);
        HttpStatus status = response.getStatusCode() != 200 ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        accountService.resetPassword(request.getEmail(), request.getNewPassword());
        return buildResponse(null, AccountResponseStatus.PASSWORD_RESET_SUCCESS);
    }

    @GetMapping("/{userId}")
    public ApiResponse<AccountResponse> getAccount(@PathVariable String userId) {
        return buildResponse(accountService.getUser(userId), AccountResponseStatus.SUCCESS);
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        accountService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return buildResponse(null, AccountResponseStatus.PASSWORD_CHANGED);
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

    @PostMapping("/ban")
    public ApiResponse<Object> banAccount(@RequestParam("user_id") String userId) {
        return ApiResponse.<Object>builder()
                .data(accountService.banUser(userId))
                .message("Ban tài khoản thành công")
                .build();
    }

    private <T> ApiResponse<T> buildResponse(T data, AccountResponseStatus responseStatus) {
        return ApiResponse.<T>builder()
                .statusCode(responseStatus.getCODE())
                .message(responseStatus.getMessage())
                .dateTime(LocalDateTime.now())
                .data(data)
                .build();
    }

    @GetMapping("/profile")
    public ApiResponse<AccountResponse> getProfile(){

        String authHeader = httpServletRequest.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new UncheckedException("Authorization not found");
        }

        String userId = jwtService.getUserId(jwtService.getToken(authHeader));

        return ApiResponse.<AccountResponse>builder()
                .data(accountService.getProfile(userId))
                .message("Ban tài khoản thành công")
                .build();
    }
}

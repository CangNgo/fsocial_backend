package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.profile.ProfileDTO;
import com.fsocial.postservice.dto.profile.ProfileResponse;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.services.AccountService;
import com.fsocial.postservice.services.JwtService;
import com.fsocial.postservice.services.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jodd.exception.UncheckedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    JwtService jwtService;
    AccountService accountService;
    ProfileService profileService;

    @GetMapping()
    public ApiResponse<AccountResponse> getProfile(HttpServletRequest httpServletRequest) {

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UncheckedException("Authorization not found");
        }

        String userId = jwtService.getUserId(jwtService.getToken(authHeader));

        return ApiResponse.<AccountResponse>builder()
                .data(accountService.getProfile(userId))
                .message("Lấy thông tin tài khoản thành công")
                .build();
    }

    @PutMapping("/update-avatar")
    public ApiResponse<ProfileResponse> updateAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        return ApiResponse.<ProfileResponse>builder()
                .data(profileService.updateAvatar(file, userId))
                .message("Cập nhật ảnh đại diện thành công")
                .build();
    }

    @PutMapping("/update-background")
    public ApiResponse<ProfileResponse> updateBackground(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        return ApiResponse.<ProfileResponse>builder()
                .data(profileService.updateBackground(file, userId))
                .message("Cập nhật ảnh nền thành công")
                .build();
    }
}

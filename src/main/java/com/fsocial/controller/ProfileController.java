package com.fsocial.controller;

import com.fsocial.dto.ApiResponse;
import com.fsocial.dto.post.PostResponse;
import com.fsocial.dto.profile.ProfileResponse;
import com.fsocial.dto.request.UpdateProfileRequest;
import com.fsocial.dto.response.AccountResponse;
import com.fsocial.services.AccountService;
import com.fsocial.services.PostService;
import com.fsocial.services.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    AccountService accountService;
    ProfileService profileService;
    PostService postService;

    @GetMapping()
    public ApiResponse<AccountResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {

        return ApiResponse.<AccountResponse>builder()
                .data(accountService.getUser(jwt.getSubject()))
                .message("Lấy thông tin tài khoản thành công")
                .build();
    }

    @GetMapping("/post")
    public ApiResponse<List<PostResponse>> getPostProfile(@AuthenticationPrincipal Jwt jwt) {
        int size = 10;
        return ApiResponse.<List<PostResponse>>builder()
                .data(postService.getPostsByUserId(jwt.getSubject(), size))
                .message("Lấy thông tin tài khoản thành công")
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<AccountResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.<AccountResponse>builder()
                .data(profileService.updatePersonalInfo(request, jwt.getSubject()))
                .message("Cập nhật thông tin cá nhân thành công")
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

//    @GetMapping("/images")
//    public ApiResponse<>
}

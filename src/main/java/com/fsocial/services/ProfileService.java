package com.fsocial.services;

import com.fsocial.dto.profile.ProfileResponse;
import com.fsocial.dto.request.UpdateProfileRequest;
import com.fsocial.dto.response.AccountResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileResponse updateAvatar(MultipartFile file, String userId);
    ProfileResponse updateBackground(MultipartFile file, String userId);
    AccountResponse updatePersonalInfo(UpdateProfileRequest request, String userId);
    String uploadImage(MultipartFile file);
}

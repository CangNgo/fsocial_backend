package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.profile.ProfileResponse;
import com.fsocial.postservice.dto.request.UpdateProfileRequest;
import com.fsocial.postservice.dto.response.AccountResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileResponse updateAvatar(MultipartFile file, String userId);
    ProfileResponse updateBackground(MultipartFile file, String userId);
    AccountResponse updatePersonalInfo(UpdateProfileRequest request, String userId);
    String uploadImage(MultipartFile file);
}

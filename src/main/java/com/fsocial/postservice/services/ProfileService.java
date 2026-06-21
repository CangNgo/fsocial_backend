package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.profile.ProfileDTO;
import com.fsocial.postservice.dto.profile.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MultipartFilter;

public interface ProfileService {

    ProfileResponse updateAvatar (MultipartFile file, String userId);
    ProfileResponse updateBackground(MultipartFile file, String userId);
    String uploadImage(MultipartFile file);
}

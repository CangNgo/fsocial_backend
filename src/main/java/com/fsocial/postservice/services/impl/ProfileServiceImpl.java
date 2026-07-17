package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.profile.ProfileResponse;
import com.fsocial.postservice.dto.request.UpdateProfileRequest;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.mapper.AccountMapper;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.services.ProfileService;
import com.fsocial.postservice.services.UploadMedia;
import com.fsocial.postservice.util.DisplayNameUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    AccountRepository accountRepository;
    AccountMapper accountMapper;
    UploadMedia uploadMedia;

    @Override
    public ProfileResponse updateAvatar(MultipartFile file, String userId) {
        String avatar = uploadImage(file);
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.UPLOAD_AVATAR_FAIL));

        account.setAvatar(avatar);
        return accountMapper.toProfileResponse(accountRepository.save(account));
    }

    @Override
    public ProfileResponse updateBackground(MultipartFile file, String userId) {
        String background = uploadImage(file);
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.UPLOAD_AVATAR_FAIL));

        account.setBackground(background);
        return accountMapper.toProfileResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse updatePersonalInfo(UpdateProfileRequest request, String userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AppException(StatusCode.USER_NOT_FOUND));

        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setDob(request.getDob());
        account.setGender(request.getGender());
        account.setAddress(request.getAddress());

        Account updatedAccount = accountRepository.save(account);

        return AccountResponse.builder()
                .id(updatedAccount.getId())
                .username(updatedAccount.getUsername())
                .firstName(updatedAccount.getFirstName())
                .lastName(updatedAccount.getLastName())
                .email(updatedAccount.getEmail())
                .dob(updatedAccount.getDob() != null ? updatedAccount.getDob().toString() : null)
                .gender(updatedAccount.getGender())
                .address(updatedAccount.getAddress())
                .displayName(DisplayNameUtils.build(updatedAccount))
                .avatar(updatedAccount.getAvatar())
                .background(updatedAccount.getBackground())
                .isKOL(updatedAccount.isKOL())
                .role(updatedAccount.getRole().getName())
                .bio(updatedAccount.getBio())
                .follower(updatedAccount.getFollower())
                .following(updatedAccount.getFollowing())
                .build();
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return uploadMedia.uploadSingleMedia(file).getUrl();
    }
}

package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.profile.ProfileDTO;
import com.fsocial.postservice.dto.profile.ProfileResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.exception.AppCheckedException;
import com.fsocial.postservice.exception.AppUnCheckedException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.mapper.AccountMapper;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.services.ProfileService;
import com.fsocial.postservice.services.UploadMedia;
import jodd.exception.UncheckedException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MultipartFilter;

import java.util.Optional;

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
        Optional<Account> account = accountRepository.findById(userId);

        if(account.isPresent()){
            account.get().setAvatar(avatar);
            return accountMapper.toProfileResponse(accountRepository.save(account.get()));
        }else {
            log.info("Lỗi khi update avatar");
            throw new AppUnCheckedException(StatusCode.UPLOAD_AVATAR_FAIL);
        }
    }

    @Override
    public ProfileResponse updateBackground(MultipartFile file, String userId) {
        String background = uploadImage(file);
        Optional<Account> account = accountRepository.findById(userId);

        if(account.isPresent()){
            account.get().setBackground(background);
            return accountMapper.toProfileResponse(accountRepository.save(account.get()));
        }else {
            log.info("Lỗi khi update avatar");
            throw new AppUnCheckedException(StatusCode.UPLOAD_AVATAR_FAIL);
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            return uploadMedia.uploadSingleMedia(file).getUrl();
        } catch (AppCheckedException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.fsocial.postservice.mapper;

import com.fsocial.postservice.dto.request.AccountRegisterRequest;
import com.fsocial.postservice.dto.request.ProfileRegisterRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileRegisterRequest toProfileRegister(AccountRegisterRequest request);
}

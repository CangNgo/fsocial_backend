package com.fsocial.mapper;

import com.fsocial.dto.request.AccountRegisterRequest;
import com.fsocial.dto.request.ProfileRegisterRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileRegisterRequest toProfileRegister(AccountRegisterRequest request);
}

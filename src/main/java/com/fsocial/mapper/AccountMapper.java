package com.fsocial.mapper;

import com.fsocial.dto.profile.ProfileDTO;
import com.fsocial.dto.profile.ProfileResponse;
import com.fsocial.dto.request.AccountRegisterRequest;
import com.fsocial.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toEntity(AccountRegisterRequest accountDTO);
//
//    @Mapping(target = "id", source = "id")
//    @Mapping(target = "username", source = "username")
//    AccountResponse toAccountResponse(Account account);
    Account toEntity(ProfileDTO profileDTO);

    @Mapping(target = "displayName", expression = "java(com.fsocial.util.DisplayNameUtils.build(account))")
    ProfileResponse toProfileResponse(Account account);
}

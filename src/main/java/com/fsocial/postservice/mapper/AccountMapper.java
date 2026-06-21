package com.fsocial.postservice.mapper;

import com.fsocial.postservice.dto.profile.ProfileDTO;
import com.fsocial.postservice.dto.profile.ProfileResponse;
import com.fsocial.postservice.dto.request.AccountRegisterRequest;
import com.fsocial.postservice.dto.response.AccountResponse;
import com.fsocial.postservice.entity.Account;
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
    ProfileResponse toProfileResponse(Account account);
}

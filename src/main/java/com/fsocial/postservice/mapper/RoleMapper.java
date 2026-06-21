package com.fsocial.postservice.mapper;

import com.fsocial.postservice.dto.request.RoleCreationRequest;
import com.fsocial.postservice.dto.response.RoleResponse;
import com.fsocial.postservice.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);
}

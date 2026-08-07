package com.fsocial.mapper;

import com.fsocial.dto.request.RoleCreationRequest;
import com.fsocial.dto.response.RoleResponse;
import com.fsocial.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreationRequest request);

    RoleResponse toRoleResponse(Role role);
}

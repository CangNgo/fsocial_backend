package com.fsocial.mapper;

import com.fsocial.dto.request.PermissionRequest;
import com.fsocial.dto.response.PermissionResponse;
import com.fsocial.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}

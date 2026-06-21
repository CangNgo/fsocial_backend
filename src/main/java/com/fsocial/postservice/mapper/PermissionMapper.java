package com.fsocial.postservice.mapper;

import com.fsocial.postservice.dto.request.PermissionRequest;
import com.fsocial.postservice.dto.response.PermissionResponse;
import com.fsocial.postservice.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}

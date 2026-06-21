package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.request.PermissionRequest;
import com.fsocial.postservice.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    List<PermissionResponse> getAllPermissions();
    void deletePermission(String name);
}

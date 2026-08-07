package com.fsocial.services;

import com.fsocial.dto.request.PermissionRequest;
import com.fsocial.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    List<PermissionResponse> getAllPermissions();
    void deletePermission(String name);
}

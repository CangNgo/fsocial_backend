package com.fsocial.services;

import com.fsocial.dto.request.RoleCreationRequest;
import com.fsocial.dto.response.RoleResponse;

import java.util.List;
import java.util.Set;

public interface RoleService {
    RoleResponse createRole(RoleCreationRequest request);
    List<RoleResponse> getAllRoles();
    RoleResponse updateRole(String roleId, Set<String> newPermissions);
    void deleteRole(String name);
}

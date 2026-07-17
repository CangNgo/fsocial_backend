package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.request.RoleCreationRequest;
import com.fsocial.postservice.dto.response.RoleResponse;
import com.fsocial.postservice.entity.Permission;
import com.fsocial.postservice.entity.Role;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.mapper.RoleMapper;
import com.fsocial.postservice.repository.PermissionRepository;
import com.fsocial.postservice.repository.RoleRepository;
import com.fsocial.postservice.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(RoleCreationRequest request) {
        Role role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }

    @Override
    public RoleResponse updateRole(String roleId, Set<String> newPermissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(AccountErrorCode.NOT_FOUND));

        var permissions = permissionRepository.findAllById(newPermissions);
        Set<Permission> permissionSet = role.getPermissions() != null ? role.getPermissions() : new HashSet<>();
        permissionSet.addAll(permissions);
        role.setPermissions(permissionSet);

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @Override
    public void deleteRole(String name) {
        roleRepository.deleteById(name);
    }
}

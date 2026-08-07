package com.fsocial.services.impl;

import com.fsocial.dto.request.RoleCreationRequest;
import com.fsocial.dto.response.RoleResponse;
import com.fsocial.entity.Permission;
import com.fsocial.entity.Role;
import com.fsocial.enums.AccountErrorCode;
import com.fsocial.exception.AppException;
import com.fsocial.mapper.RoleMapper;
import com.fsocial.repository.PermissionRepository;
import com.fsocial.repository.RoleRepository;
import com.fsocial.services.RoleService;
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

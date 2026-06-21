package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.request.PermissionRequest;
import com.fsocial.postservice.dto.response.PermissionResponse;
import com.fsocial.postservice.entity.Permission;
import com.fsocial.postservice.mapper.PermissionMapper;
import com.fsocial.postservice.repository.PermissionRepository;
import com.fsocial.postservice.services.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {

    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Override
    public PermissionResponse createPermission(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    @Override
    public void deletePermission(String name) {
        permissionRepository.deleteById(name);
    }
}

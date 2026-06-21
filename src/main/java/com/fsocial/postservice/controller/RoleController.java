package com.fsocial.postservice.controller;

import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.request.RoleCreationRequest;
import com.fsocial.postservice.dto.response.RoleResponse;
import com.fsocial.postservice.enums.AccountErrorCode;
import com.fsocial.postservice.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/role")
public class RoleController {

    RoleService roleService;

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody RoleCreationRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .statusCode(AccountErrorCode.OK.getCode())
                .message("Create new Role success.")
                .data(roleService.createRole(request))
                .build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<RoleResponse>> getAllRole() {
        return ApiResponse.<List<RoleResponse>>builder()
                .statusCode(AccountErrorCode.OK.getCode())
                .message("Get all Role success.")
                .data(roleService.getAllRoles())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roleId}")
    public ApiResponse<RoleResponse> updateRole(@PathVariable String roleId, @RequestBody Set<String> permissions) {
        return ApiResponse.<RoleResponse>builder()
                .statusCode(AccountErrorCode.OK.getCode())
                .message("Update role success.")
                .data(roleService.updateRole(roleId, permissions))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> deleteRole(@PathVariable String roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.<Void>builder()
                .statusCode(AccountErrorCode.OK.getCode())
                .message("Delete role success.")
                .build();
    }
}

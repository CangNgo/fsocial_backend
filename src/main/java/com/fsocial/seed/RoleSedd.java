package com.fsocial.seed;

import com.fsocial.entity.Permission;
import com.fsocial.entity.Role;
import com.fsocial.repository.PermissionRepository;
import com.fsocial.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Profile("role-seed")
public class RoleSedd implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        Permission read = seedPermission("READ", "Xem dữ liệu");
        Permission write = seedPermission("WRITE", "Tạo/sửa dữ liệu");
        Permission delete = seedPermission("DELETE", "Xóa dữ liệu");
        Permission manageUsers = seedPermission("MANAGE_USERS", "Quản lý tài khoản người dùng");

        seedRole("USER", "Người dùng thông thường", Set.of(read, write));
        seedRole("ADMIN", "Quản trị viên", Set.of(read, write, delete, manageUsers));
    }

    private Permission seedPermission(String name, String description) {
        return permissionRepository.findById(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name, description)));
    }

    private void seedRole(String name, String description, Set<Permission> permissions) {
        roleRepository.findByName(name).orElseGet(() -> roleRepository.save(
                Role.builder()
                        .name(name)
                        .description(description)
                        .permissions(new HashSet<>(permissions))
                        .build()
        ));
    }
}

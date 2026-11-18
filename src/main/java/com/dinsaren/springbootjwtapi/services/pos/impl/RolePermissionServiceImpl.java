package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.RolePermission;
import com.dinsaren.springbootjwtapi.payload.pos.RoleUpdateRequest;
import com.dinsaren.springbootjwtapi.repository.pos.RolePermissionRepository;
import com.dinsaren.springbootjwtapi.services.pos.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAllByOrderByRoleAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDefaultPermissions(String role) {
        String cleanRole = role != null ? role.toUpperCase().replace("ROLE_", "") : "CASHIER";
        return rolePermissionRepository.findByRole(cleanRole)
                .map(RolePermission::getPages)
                .orElseGet(() -> {
                    if ("ADMIN".equalsIgnoreCase(cleanRole)) {
                        return List.of("/", "/pos", "/products", "/categories", "/ingredients", "/discounts",
                                "/inventory", "/purchases", "/suppliers", "/customers", "/orders",
                                "/reports", "/users", "/settings", "/roles");
                    } else if ("MANAGER".equalsIgnoreCase(cleanRole)) {
                        return List.of("/", "/pos", "/products", "/categories", "/ingredients", "/discounts",
                                "/inventory", "/purchases", "/suppliers", "/customers", "/orders");
                    } else {
                        return List.of("/pos");
                    }
                });
    }

    @Override
    @Transactional
    public Map<String, Object> updateRolePermissions(RoleUpdateRequest request) {
        if (request.getRole() == null || request.getPages() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request payload");
        }

        String cleanRole = request.getRole().toUpperCase().replace("ROLE_", "");
        RolePermission rolePermission = rolePermissionRepository.findByRole(cleanRole)
                .orElse(new RolePermission(cleanRole, request.getPages()));

        rolePermission.setPages(request.getPages());
        RolePermission saved = rolePermissionRepository.save(rolePermission);

        return Map.of("success", true, "updatedPermission", saved);
    }
}

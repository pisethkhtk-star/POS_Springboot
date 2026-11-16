package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.RolePermission;
import com.dinsaren.springbootjwtapi.payload.pos.RoleUpdateRequest;
import com.dinsaren.springbootjwtapi.services.pos.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/roles")
@Slf4j
@Tag(name = "POS Roles", description = "Manage role page permissions")
@RequiredArgsConstructor
public class PosRoleController {

    private final RolePermissionService rolePermissionService;

    @Operation(summary = "Get all role permissions")
    @GetMapping
    public ResponseEntity<List<RolePermission>> getAll() {
        return ResponseEntity.ok(rolePermissionService.getAllRolePermissions());
    }

    @Operation(summary = "Update role permissions")
    @PostMapping
    public ResponseEntity<Map<String, Object>> updatePermissions(@RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(rolePermissionService.updateRolePermissions(request));
    }
}

package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.RolePermission;
import com.dinsaren.springbootjwtapi.payload.pos.RoleUpdateRequest;

import java.util.List;
import java.util.Map;

public interface RolePermissionService {
    List<RolePermission> getAllRolePermissions();
    List<String> getDefaultPermissions(String role);
    Map<String, Object> updateRolePermissions(RoleUpdateRequest request);
}

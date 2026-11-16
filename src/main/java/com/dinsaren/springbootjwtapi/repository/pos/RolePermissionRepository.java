package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    Optional<RolePermission> findByRole(String role);
    List<RolePermission> findAllByOrderByRoleAsc();
}

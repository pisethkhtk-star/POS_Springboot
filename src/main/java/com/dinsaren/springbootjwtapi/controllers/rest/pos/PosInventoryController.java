package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.payload.pos.InventoryAdjustRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/inventory")
@Slf4j
@Tag(name = "POS Inventory", description = "Track stock adjustments and audit history")
@RequiredArgsConstructor
public class PosInventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get all inventory audit logs")
    @GetMapping
    public ResponseEntity<List<InventoryLog>> getAll() {
        return ResponseEntity.ok(inventoryService.getAllLogs());
    }

    @Operation(summary = "Adjust stock for product or ingredient")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adjust(
            @RequestBody InventoryAdjustRequest request,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user) {
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN")
                            || a.getAuthority().equalsIgnoreCase("ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access denied: Only ADMIN has permission to adjust stock"));
            }
        }
        Integer userId = user != null ? user.getId() : 1;
        String userName = user != null ? user.getUsername() : "Admin";
        return ResponseEntity.ok(inventoryService.adjustStock(request, userId, userName));
    }
}

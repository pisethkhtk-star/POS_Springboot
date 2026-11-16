package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Unit;
import com.dinsaren.springbootjwtapi.models.pos.UnitType;
import com.dinsaren.springbootjwtapi.payload.pos.UnitRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.PosUnitService;
import com.dinsaren.springbootjwtapi.services.pos.UnitConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/units")
@Slf4j
@Tag(name = "POS Units", description = "Manage measurement units for ingredients and products")
@RequiredArgsConstructor
public class PosUnitController {

    private final PosUnitService unitService;
    private final UnitConversionService unitConversionService;

    @Operation(summary = "Get all units or filter by unit type (WEIGHT, VOLUME, COUNT)")
    @GetMapping
    public ResponseEntity<List<Unit>> getAll(@RequestParam(required = false) UnitType type) {
        if (type != null) {
            return ResponseEntity.ok(unitService.getUnitsByType(type));
        }
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @Operation(summary = "Get unit by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Unit> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @Operation(summary = "Convert quantity between two unit symbols")
    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
        @RequestParam BigDecimal quantity,
        @RequestParam String from,
        @RequestParam String to
    ) {
        Unit fromUnit = unitConversionService.findBySymbol(from)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + from));
        Unit toUnit = unitConversionService.findBySymbol(to)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + to));

        BigDecimal result = unitConversionService.convert(quantity, fromUnit, toUnit);
        return ResponseEntity.ok(Map.of(
            "quantity", quantity,
            "from", from,
            "to", to,
            "result", result
        ));
    }

    @Operation(summary = "Create a new unit")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(
        @RequestBody UnitRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied: Only ADMIN can create units"));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(unitService.createUnit(request));
    }

    @Operation(summary = "Update a unit")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(
        @PathVariable Integer id,
        @RequestBody UnitRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied: Only ADMIN can update units"));
            }
        }
        return ResponseEntity.ok(unitService.updateUnit(id, request));
    }

    @Operation(summary = "Delete a unit")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
        @PathVariable Integer id,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied: Only ADMIN can delete units"));
            }
        }
        unitService.deleteUnit(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

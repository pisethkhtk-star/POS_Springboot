package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Supplier;
import com.dinsaren.springbootjwtapi.payload.pos.SupplierRequest;
import com.dinsaren.springbootjwtapi.services.pos.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/suppliers")
@Slf4j
@Tag(name = "POS Suppliers", description = "Manage POS ingredient suppliers")
@RequiredArgsConstructor
public class PosSupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "Search suppliers")
    @GetMapping
    public ResponseEntity<List<Supplier>> search(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(supplierService.searchSuppliers(search));
    }

    @Operation(summary = "Get supplier by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @Operation(summary = "Create supplier")
    @PostMapping
    public ResponseEntity<Supplier> create(@RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @Operation(summary = "Update supplier")
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable Integer id, @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @Operation(summary = "Delete supplier")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Integer id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

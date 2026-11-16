package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosCustomer;
import com.dinsaren.springbootjwtapi.payload.pos.CustomerRequest;
import com.dinsaren.springbootjwtapi.services.pos.PosCustomerService;
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
@RequestMapping("/api/customers")
@Slf4j
@Tag(name = "POS Customers", description = "Manage POS customer loyalty profiles")
@RequiredArgsConstructor
public class PosCustomerController {

    private final PosCustomerService customerService;

    @Operation(summary = "Search customers")
    @GetMapping
    public ResponseEntity<List<PosCustomer>> search(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(customerService.searchCustomers(search));
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PosCustomer> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Create customer")
    @PostMapping
    public ResponseEntity<PosCustomer> create(@RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @Operation(summary = "Update customer")
    @PutMapping("/{id}")
    public ResponseEntity<PosCustomer> update(@PathVariable Integer id, @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @Operation(summary = "Delete customer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

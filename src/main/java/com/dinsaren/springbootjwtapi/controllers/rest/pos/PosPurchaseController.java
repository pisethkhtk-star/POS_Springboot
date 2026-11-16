package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Purchase;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseReceiveRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/purchases")
@Slf4j
@Tag(name = "POS Purchases", description = "Stock IN raw ingredients and direct products from suppliers")
@RequiredArgsConstructor
public class PosPurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "Get all purchases with optional status filter (PENDING, RECEIVED, CANCELLED)")
    @GetMapping
    public ResponseEntity<List<Purchase>> getAll(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(purchaseService.getPurchasesByStatus(status));
        }
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    @Operation(summary = "Get purchase by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @Operation(summary = "Create purchase order (saves in PENDING status, stock NOT added yet)")
    @PostMapping
    public ResponseEntity<Purchase> create(
        @RequestBody PurchaseCreateRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchase(request, userId));
    }

    @Operation(summary = "Receive purchase items into stock (adds stock, updates weighted cost & expiry)")
    @PostMapping("/{id}/receive")
    public ResponseEntity<Purchase> receive(
        @PathVariable Integer id,
        @RequestBody(required = false) PurchaseReceiveRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.ok(purchaseService.receivePurchase(id, request, userId));
    }

    @Operation(summary = "Cancel pending purchase order")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Purchase> cancel(
        @PathVariable Integer id,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.ok(purchaseService.cancelPurchase(id, userId));
    }
}

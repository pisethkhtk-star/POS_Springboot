package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Purchase;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseCreateRequest;
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
@Tag(name = "POS Purchases", description = "Stock IN raw ingredients from suppliers")
@RequiredArgsConstructor
public class PosPurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "Get all purchases")
    @GetMapping
    public ResponseEntity<List<Purchase>> getAll() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    @Operation(summary = "Get purchase by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Purchase> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @Operation(summary = "Create raw ingredient purchase order")
    @PostMapping
    public ResponseEntity<Purchase> create(
        @RequestBody PurchaseCreateRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseService.createPurchase(request, userId));
    }
}

package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Discount;
import com.dinsaren.springbootjwtapi.payload.pos.DiscountRequest;
import com.dinsaren.springbootjwtapi.services.pos.DiscountService;
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
@RequestMapping("/api/discounts")
@Slf4j
@Tag(name = "POS Discounts", description = "Manage POS discount promotions")
@RequiredArgsConstructor
public class PosDiscountController {

    private final DiscountService discountService;

    @Operation(summary = "Get all discounts")
    @GetMapping
    public ResponseEntity<List<Discount>> getAll() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }

    @Operation(summary = "Get discount by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Discount> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @Operation(summary = "Create discount")
    @PostMapping
    public ResponseEntity<Discount> create(@RequestBody DiscountRequest request) {
        return ResponseEntity.ok(discountService.createDiscount(request));
    }

    @Operation(summary = "Update discount")
    @PutMapping("/{id}")
    public ResponseEntity<Discount> update(@PathVariable Integer id, @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(discountService.updateDiscount(id, request));
    }

    @Operation(summary = "Delete discount")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(Map.of("message", "Discount deleted successfully"));
    }
}

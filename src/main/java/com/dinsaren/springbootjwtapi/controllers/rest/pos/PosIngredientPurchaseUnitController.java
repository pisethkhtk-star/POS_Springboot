package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientPurchaseUnitRequest;
import com.dinsaren.springbootjwtapi.services.pos.IngredientPurchaseUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ingredients/{ingredientId}/purchase-units")
@Slf4j
@Tag(name = "POS Ingredient Purchase Units", description = "Manage per-ingredient custom purchase units (e.g. Bag, Box, Bottle) and conversions")
@RequiredArgsConstructor
public class PosIngredientPurchaseUnitController {

    private final IngredientPurchaseUnitService purchaseUnitService;

    @Operation(summary = "Get all configured purchase units for an ingredient")
    @GetMapping
    public ResponseEntity<List<IngredientPurchaseUnit>> getUnits(@PathVariable Integer ingredientId) {
        return ResponseEntity.ok(purchaseUnitService.getUnits(ingredientId));
    }

    @Operation(summary = "Add a custom purchase unit for an ingredient")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngredientPurchaseUnit> addUnit(
        @PathVariable Integer ingredientId,
        @RequestBody IngredientPurchaseUnitRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseUnitService.addUnit(ingredientId, request));
    }

    @Operation(summary = "Delete a custom purchase unit from an ingredient")
    @DeleteMapping("/{unitId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> deleteUnit(
        @PathVariable Integer ingredientId,
        @PathVariable Integer unitId
    ) {
        purchaseUnitService.deleteUnit(ingredientId, unitId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

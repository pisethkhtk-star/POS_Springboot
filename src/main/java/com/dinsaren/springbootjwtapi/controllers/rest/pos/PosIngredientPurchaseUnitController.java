package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientPurchaseUnitRequest;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientPurchaseUnitRepository;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ingredients/{ingredientId}/purchase-units")
@Slf4j
@Tag(name = "POS Ingredient Purchase Units", description = "Manage per-ingredient custom purchase units (e.g. Bag, Box, Bottle) and conversions")
@RequiredArgsConstructor
public class PosIngredientPurchaseUnitController {

    private final IngredientPurchaseUnitRepository purchaseUnitRepository;
    private final IngredientRepository ingredientRepository;

    @Operation(summary = "Get all configured purchase units for an ingredient")
    @GetMapping
    public ResponseEntity<List<IngredientPurchaseUnit>> getUnits(@PathVariable Integer ingredientId) {
        return ResponseEntity.ok(purchaseUnitRepository.findByIngredientId(ingredientId));
    }

    @Operation(summary = "Add a custom purchase unit for an ingredient")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IngredientPurchaseUnit> addUnit(
        @PathVariable Integer ingredientId,
        @RequestBody IngredientPurchaseUnitRequest request
    ) {
        ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found: " + ingredientId));

        if (request.getUnitName() == null || request.getUnitName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit name is required");
        }
        if (request.getFactorToBase() == null || request.getFactorToBase().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "factorToBase must be greater than zero");
        }

        IngredientPurchaseUnit pu = new IngredientPurchaseUnit(
            ingredientId,
            request.getUnitName().trim(),
            request.getUnitNameKm(),
            request.getFactorToBase(),
            request.getDefaultCost()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseUnitRepository.save(pu));
    }

    @Operation(summary = "Delete a custom purchase unit from an ingredient")
    @DeleteMapping("/{unitId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> deleteUnit(
        @PathVariable Integer ingredientId,
        @PathVariable Integer unitId
    ) {
        IngredientPurchaseUnit pu = purchaseUnitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase unit not found"));
        purchaseUnitRepository.delete(pu);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

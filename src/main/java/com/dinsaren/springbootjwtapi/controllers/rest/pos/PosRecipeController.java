package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.payload.pos.ProductRecipeResponse;
import com.dinsaren.springbootjwtapi.payload.pos.RecipeItemRequest;
import com.dinsaren.springbootjwtapi.services.pos.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/recipes")
@Slf4j
@Tag(name = "POS Recipes", description = "Manage product recipes, unit conversion, and automated COGS calculation")
@RequiredArgsConstructor
public class PosRecipeController {

    private final RecipeService recipeService;

    @Operation(summary = "Get detailed product recipe with COGS breakdown")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductRecipeResponse> getRecipe(@PathVariable Integer productId) {
        return ResponseEntity.ok(recipeService.getProductRecipe(productId));
    }

    @Operation(summary = "Save or replace product recipe and recalculate COGS")
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductRecipeResponse> saveRecipe(
        @PathVariable Integer productId,
        @RequestBody List<RecipeItemRequest> items
    ) {
        return ResponseEntity.ok(recipeService.saveRecipe(productId, items));
    }

    @Operation(summary = "Calculate and return product COGS from current ingredient costs")
    @GetMapping("/{productId}/cogs")
    public ResponseEntity<Map<String, Object>> getCOGS(@PathVariable Integer productId) {
        BigDecimal cogs = recipeService.calculateProductCOGS(productId);
        return ResponseEntity.ok(Map.of(
            "productId", productId,
            "cogs", cogs
        ));
    }
}

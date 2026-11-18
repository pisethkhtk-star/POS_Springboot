package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.models.pos.StockMovementType;
import com.dinsaren.springbootjwtapi.payload.pos.StockMovementRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/stock")
@Slf4j
@Tag(name = "POS Stock & Inventory", description = "Stock movements (PURCHASE, SALE, WASTE, ADJUSTMENT) and low-stock alerts")
@RequiredArgsConstructor
public class PosStockController {

    private final StockService stockService;

    @Operation(summary = "Get ingredients below minimum stock threshold (low-stock alerts)")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Ingredient>> getLowStock() {
        return ResponseEntity.ok(stockService.getLowStockIngredients());
    }

    @Operation(summary = "Record ingredient waste (spoilage, damaged, expired)")
    @PostMapping("/waste")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Ingredient> recordWaste(
        @RequestBody StockMovementRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        Ingredient updated = stockService.recordWaste(
            request.getIngredientId(),
            request.getQuantity(),
            request.getUnit(),
            request.getReason() != null ? request.getReason() : request.getNote(),
            userId
        );
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Adjust physical stock count")
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Ingredient> adjustStock(
        @RequestBody StockMovementRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        Ingredient updated = stockService.adjustStock(
            request.getIngredientId(),
            request.getQuantity(),
            request.getUnit(),
            request.getNote(),
            userId
        );
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get stock movements audit log (PURCHASE, SALE, WASTE, ADJUSTMENT)")
    @GetMapping("/movements")
    public ResponseEntity<List<InventoryLog>> getMovements(
        @RequestParam(required = false) StockMovementType type
    ) {
        return ResponseEntity.ok(stockService.getMovements(type));
    }
}

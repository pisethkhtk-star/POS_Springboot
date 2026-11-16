package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.models.pos.ProductIngredient;
import com.dinsaren.springbootjwtapi.payload.pos.ProductRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.PosProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
@Slf4j
@Tag(name = "POS Products", description = "Manage POS products and recipe ingredients")
@RequiredArgsConstructor
public class PosProductController {

    private final PosProductService productService;

    @Operation(summary = "Search products by query or category")
    @GetMapping
    public ResponseEntity<List<PosProduct>> search(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String categoryId
    ) {
        return ResponseEntity.ok(productService.searchProducts(query, categoryId));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PosProduct> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Create product")
    @PostMapping
    public ResponseEntity<PosProduct> create(
        @RequestBody ProductRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request, userId));
    }

    @Operation(summary = "Update product")
    @PutMapping("/{id}")
    public ResponseEntity<PosProduct> update(
        @PathVariable Integer id,
        @RequestBody ProductRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.ok(productService.updateProduct(id, request, userId));
    }

    @Operation(summary = "Delete product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @Operation(summary = "Bulk import products")
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> bulkImport(
        @RequestBody Map<String, List<Map<String, Object>>> body,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        List<Map<String, Object>> products = body.get("products");
        return ResponseEntity.ok(productService.bulkImport(products, userId));
    }

    @Operation(summary = "Get product recipe ingredients")
    @GetMapping("/{id}/ingredients")
    public ResponseEntity<List<ProductIngredient>> getIngredients(@PathVariable Integer id) {
        PosProduct product = productService.getProductById(id);
        return ResponseEntity.ok(product.getIngredients());
    }
}

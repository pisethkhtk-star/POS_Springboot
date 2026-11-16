package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ingredients")
@Slf4j
@Tag(name = "POS Ingredients", description = "Manage POS raw ingredients and inventory tracking")
@RequiredArgsConstructor
public class PosIngredientController {

    private final IngredientService ingredientService;

    @Operation(summary = "Search ingredients")
    @GetMapping
    public ResponseEntity<List<Ingredient>> search(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean lowStock
    ) {
        String activeQuery = query != null ? query : search;
        return ResponseEntity.ok(ingredientService.searchIngredients(activeQuery, lowStock));
    }

    @Operation(summary = "Get ingredient by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @Operation(summary = "Create ingredient")
    @PostMapping
    public ResponseEntity<Ingredient> create(
        @RequestBody IngredientRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        String userName = user != null ? user.getUsername() : "Admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.createIngredient(request, userId, userName));
    }

    @Operation(summary = "Update ingredient")
    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> update(
        @PathVariable Integer id,
        @RequestBody IngredientRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        String userName = user != null ? user.getUsername() : "Admin";
        return ResponseEntity.ok(ingredientService.updateIngredient(id, request, userId, userName));
    }

    @Operation(summary = "Delete ingredient")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
        @PathVariable Integer id,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied: Only ADMIN has permission to delete ingredients"));
            }
        }
        ingredientService.deleteIngredient(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.Category;
import com.dinsaren.springbootjwtapi.payload.pos.CategoryRequest;
import com.dinsaren.springbootjwtapi.services.pos.PosCategoryService;
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
@RequestMapping("/api/categories")
@Slf4j
@Tag(name = "POS Categories", description = "Manage POS product categories")
@RequiredArgsConstructor
public class PosCategoryController {

    private final PosCategoryService categoryService;

    @Operation(summary = "Get all categories")
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Create category")
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Integer id, @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

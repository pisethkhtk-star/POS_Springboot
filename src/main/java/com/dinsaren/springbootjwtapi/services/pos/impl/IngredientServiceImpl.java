package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.models.pos.ProductIngredient;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientRequest;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import com.dinsaren.springbootjwtapi.services.pos.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final PosProductRepository posProductRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> searchIngredients(String query, Boolean lowStock) {
        List<Ingredient> list;
        if (query != null && !query.trim().isEmpty()) {
            list = ingredientRepository.searchIngredients(query.trim());
        } else {
            list = ingredientRepository.findAllByOrderByNameAsc();
        }

        if (Boolean.TRUE.equals(lowStock)) {
            list = list.stream()
                    .filter(i -> i.getStock().compareTo(i.getMinStock()) <= 0)
                    .collect(Collectors.toList());
        }

        for (Ingredient ing : list) {
            long pCount = productIngredientRepository.countByIngredientId(ing.getId());
            long puCount = purchaseItemRepository.countByIngredientId(ing.getId());
            ing.setCount(Map.of("productItems", pCount, "purchaseItems", puCount));
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Ingredient getIngredientById(Integer id) {
        Ingredient ing = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));
        long pCount = productIngredientRepository.countByIngredientId(ing.getId());
        long puCount = purchaseItemRepository.countByIngredientId(ing.getId());
        ing.setCount(Map.of("productItems", pCount, "purchaseItems", puCount));
        return ing;
    }

    private String generateNextIngredientCode() {
        long count = ingredientRepository.count();
        return String.format("ING-%03d", count + 1);
    }

    @Override
    @Transactional
    public Ingredient createIngredient(IngredientRequest request, Integer currentUserId, String currentUserName) {
        if (request.getName() == null || request.getName().trim().isEmpty() ||
            request.getUnit() == null || request.getUnit().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields: name and unit are required");
        }

        String code = request.getCode() != null ? request.getCode().trim() : "";
        if (code.isEmpty()) {
            code = generateNextIngredientCode();
        }

        if (ingredientRepository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient code already exists");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName().trim());
        ingredient.setCode(code);
        ingredient.setUnit(request.getUnit().trim());
        ingredient.setStock(request.getStock() != null ? request.getStock() : BigDecimal.ZERO);
        ingredient.setMinStock(request.getMinStock() != null ? request.getMinStock() : new BigDecimal("5.00"));
        ingredient.setCost(request.getCost() != null ? request.getCost() : BigDecimal.ZERO);

        Ingredient saved = ingredientRepository.save(ingredient);

        if (saved.getStock().compareTo(BigDecimal.ZERO) > 0) {
            InventoryLog log = new InventoryLog();
            log.setIngredientId(saved.getId());
            log.setType("PURCHASE_IN");
            log.setQuantity(saved.getStock());
            log.setBeforeQty(BigDecimal.ZERO);
            log.setAfterQty(saved.getStock());
            log.setNote("Initial ingredient stock set" + (currentUserName != null ? " by " + currentUserName : ""));
            log.setUserId(currentUserId);
            inventoryLogRepository.save(log);
        }

        saved.setCount(Map.of("productItems", 0L, "purchaseItems", 0L));
        return saved;
    }

    @Override
    @Transactional
    public Ingredient updateIngredient(Integer id, IngredientRequest request, Integer currentUserId, String currentUserName) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            ingredient.setName(request.getName().trim());
        }
        if (request.getUnit() != null && !request.getUnit().trim().isEmpty()) {
            ingredient.setUnit(request.getUnit().trim());
        }
        if (request.getCode() != null && !request.getCode().trim().isEmpty() && !request.getCode().trim().equals(ingredient.getCode())) {
            String newCode = request.getCode().trim();
            if (ingredientRepository.existsByCode(newCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient code already exists");
            }
            ingredient.setCode(newCode);
        }

        BigDecimal oldStock = ingredient.getStock();
        BigDecimal newStock = request.getStock() != null ? request.getStock() : oldStock;

        if (request.getStock() != null && newStock.compareTo(oldStock) != 0) {
            BigDecimal diff = newStock.subtract(oldStock).abs();
            ingredient.setStock(newStock);

            InventoryLog log = new InventoryLog();
            log.setIngredientId(id);
            log.setType("ADJUSTMENT");
            log.setQuantity(diff);
            log.setBeforeQty(oldStock);
            log.setAfterQty(newStock);
            log.setNote("Manual stock adjustment" + (currentUserName != null ? " by " + currentUserName : ""));
            log.setUserId(currentUserId);
            inventoryLogRepository.save(log);
        }

        if (request.getMinStock() != null) {
            ingredient.setMinStock(request.getMinStock());
        }

        BigDecimal oldCost = ingredient.getCost();
        BigDecimal newCost = request.getCost() != null ? request.getCost() : oldCost;

        if (request.getCost() != null) {
            ingredient.setCost(newCost);
        }

        Ingredient saved = ingredientRepository.save(ingredient);

        // If cost changed, recalculate cost of all products that use this ingredient
        if (request.getCost() != null && newCost.compareTo(oldCost) != 0) {
            List<ProductIngredient> recipeLinks = productIngredientRepository.findAll();
            for (ProductIngredient pi : recipeLinks) {
                if (pi.getIngredient() != null && pi.getIngredient().getId().equals(id)) {
                    PosProduct product = pi.getProduct();
                    if (product != null && product.getHasRecipe()) {
                        BigDecimal totalCost = BigDecimal.ZERO;
                        List<ProductIngredient> items = productIngredientRepository.findByProductId(product.getId());
                        for (ProductIngredient item : items) {
                            BigDecimal itemCost = item.getIngredient().getId().equals(id) ? newCost : item.getIngredient().getCost();
                            totalCost = totalCost.add(itemCost.multiply(item.getQuantity()));
                        }
                        product.setCost(totalCost.setScale(2, RoundingMode.HALF_UP));
                        posProductRepository.save(product);
                    }
                }
            }
        }

        long pCount = productIngredientRepository.countByIngredientId(saved.getId());
        long puCount = purchaseItemRepository.countByIngredientId(saved.getId());
        saved.setCount(Map.of("productItems", pCount, "purchaseItems", puCount));
        return saved;
    }

    @Override
    @Transactional
    public void deleteIngredient(Integer id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        long pCount = productIngredientRepository.countByIngredientId(id);
        long puCount = purchaseItemRepository.countByIngredientId(id);

        if (pCount > 0 || puCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete ingredient that is linked to products or purchase items");
        }

        inventoryLogRepository.deleteByIngredientId(id);
        ingredientRepository.delete(ingredient);
    }
}

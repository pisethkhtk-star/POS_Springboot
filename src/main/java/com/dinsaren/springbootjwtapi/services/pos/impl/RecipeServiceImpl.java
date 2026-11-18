package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.models.pos.ProductIngredient;
import com.dinsaren.springbootjwtapi.payload.pos.ProductRecipeResponse;
import com.dinsaren.springbootjwtapi.payload.pos.RecipeItemDTO;
import com.dinsaren.springbootjwtapi.payload.pos.RecipeItemRequest;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientRepository;
import com.dinsaren.springbootjwtapi.repository.pos.PosProductRepository;
import com.dinsaren.springbootjwtapi.repository.pos.ProductIngredientRepository;
import com.dinsaren.springbootjwtapi.services.pos.RecipeService;
import com.dinsaren.springbootjwtapi.services.pos.UnitConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeServiceImpl implements RecipeService {

    private final PosProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final UnitConversionService unitConversionService;

    @Override
    @Transactional(readOnly = true)
    public ProductRecipeResponse getProductRecipe(Integer productId) {
        PosProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + productId));

        List<ProductIngredient> recipeItems = productIngredientRepository.findByProductId(productId);
        List<RecipeItemDTO> dtoList = new ArrayList<>();
        BigDecimal totalCOGS = BigDecimal.ZERO;

        for (ProductIngredient item : recipeItems) {
            Ingredient ing = item.getIngredient();
            String recipeUnit = item.getUnit() != null ? item.getUnit() : ing.getUnit();
            BigDecimal recipeQty = item.getQuantity();

            BigDecimal baseQty = unitConversionService.convertToBase(recipeQty, recipeUnit, ing.getUnit());
            BigDecimal ingUnitCost = ing.getCost() != null ? ing.getCost() : BigDecimal.ZERO;
            BigDecimal subtotalCost = baseQty.multiply(ingUnitCost).setScale(4, RoundingMode.HALF_UP);
            totalCOGS = totalCOGS.add(subtotalCost);

            dtoList.add(RecipeItemDTO.builder()
                    .ingredientId(ing.getId())
                    .ingredientName(ing.getName())
                    .ingredientCode(ing.getCode())
                    .recipeQuantity(recipeQty)
                    .recipeUnit(recipeUnit)
                    .baseQuantity(baseQty)
                    .baseUnit(ing.getUnit())
                    .unitCost(ingUnitCost)
                    .subtotalCost(subtotalCost)
                    .build());
        }

        totalCOGS = totalCOGS.setScale(2, RoundingMode.HALF_UP);

        return ProductRecipeResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productCode(product.getCode())
                .productPrice(product.getPrice())
                .cogs(totalCOGS)
                .items(dtoList)
                .build();
    }

    @Override
    @Transactional
    public ProductRecipeResponse saveRecipe(Integer productId, List<RecipeItemRequest> items) {
        PosProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));

        productIngredientRepository.deleteByProductId(productId);

        if (items == null || items.isEmpty()) {
            product.setHasRecipe(false);
            productRepository.save(product);
            return getProductRecipe(productId);
        }

        List<ProductIngredient> newItems = new ArrayList<>();
        for (RecipeItemRequest req : items) {
            if (req.getIngredientId() == null || req.getQuantity() == null || req.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient not found: " + req.getIngredientId()));

            String unit = req.getUnit() != null && !req.getUnit().trim().isEmpty()
                    ? req.getUnit().trim()
                    : ingredient.getUnit();

            ProductIngredient pi = new ProductIngredient(product, ingredient, req.getQuantity(), unit);
            newItems.add(pi);
        }

        productIngredientRepository.saveAll(newItems);
        product.setHasRecipe(!newItems.isEmpty());

        BigDecimal computedCOGS = calculateProductCOGS(productId);
        product.setCost(computedCOGS);
        productRepository.save(product);

        log.info("Saved recipe for product '{}' ({} ingredients) - Computed COGS: ${}",
                product.getName(), newItems.size(), computedCOGS);

        return getProductRecipe(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateProductCOGS(Integer productId) {
        List<ProductIngredient> recipeItems = productIngredientRepository.findByProductId(productId);
        BigDecimal totalCOGS = BigDecimal.ZERO;

        for (ProductIngredient item : recipeItems) {
            Ingredient ing = item.getIngredient();
            String recipeUnit = item.getUnit() != null ? item.getUnit() : ing.getUnit();
            BigDecimal baseQty = unitConversionService.convertToBase(item.getQuantity(), recipeUnit, ing.getUnit());
            BigDecimal ingCost = ing.getCost() != null ? ing.getCost() : BigDecimal.ZERO;
            totalCOGS = totalCOGS.add(baseQty.multiply(ingCost));
        }

        return totalCOGS.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void recalculateAllProductsUsingIngredient(Integer ingredientId) {
        List<ProductIngredient> links = productIngredientRepository.findByIngredientId(ingredientId);
        for (ProductIngredient link : links) {
            PosProduct product = link.getProduct();
            if (product != null) {
                BigDecimal newCOGS = calculateProductCOGS(product.getId());
                product.setCost(newCOGS);
                productRepository.save(product);
                log.info("Updated product '{}' COGS to ${} after ingredient #{} cost change",
                        product.getName(), newCOGS, ingredientId);
            }
        }
    }
}

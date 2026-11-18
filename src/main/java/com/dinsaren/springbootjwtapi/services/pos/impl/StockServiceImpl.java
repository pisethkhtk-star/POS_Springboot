package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.*;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import com.dinsaren.springbootjwtapi.services.pos.RecipeService;
import com.dinsaren.springbootjwtapi.services.pos.StockService;
import com.dinsaren.springbootjwtapi.services.pos.UnitConversionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final IngredientRepository ingredientRepository;
    private final PosProductRepository productRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final IngredientPurchaseUnitRepository purchaseUnitRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final UnitConversionService unitConversionService;
    private final RecipeService recipeService;

    @Override
    @Transactional
    public void deductStockForOrder(PosOrder order, Integer currentUserId) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        for (PosOrderItem item : order.getItems()) {
            PosProduct product = item.getProduct();
            int orderQty = item.getQuantity();

            if (product.getHasRecipe() && product.getIngredients() != null && !product.getIngredients().isEmpty()) {
                // Deduct stock for all recipe ingredients using FEFO
                for (ProductIngredient pi : product.getIngredients()) {
                    Ingredient ingredient = ingredientRepository.findById(pi.getIngredient().getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

                    String recipeUnit = pi.getUnit() != null ? pi.getUnit() : ingredient.getUnit();
                    BigDecimal perServingBaseQty = unitConversionService.convertToBase(pi.getQuantity(), recipeUnit, ingredient.getUnit());
                    BigDecimal totalDeductBaseQty = perServingBaseQty.multiply(new BigDecimal(orderQty)).setScale(4, RoundingMode.HALF_UP);

                    BigDecimal oldStock = ingredient.getStock() != null ? ingredient.getStock() : BigDecimal.ZERO;
                    if (oldStock.compareTo(totalDeductBaseQty) < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Insufficient stock for ingredient: " + ingredient.getName() +
                                ". Current: " + oldStock + " " + ingredient.getUnit() +
                                ", Required: " + totalDeductBaseQty + " " + ingredient.getUnit());
                    }

                    BigDecimal newStock = oldStock.subtract(totalDeductBaseQty).setScale(4, RoundingMode.HALF_UP);
                    ingredient.setStock(newStock);

                    // FEFO deduction: deduct from earliest expiring batches first
                    List<PurchaseItem> batches = purchaseItemRepository.findActiveBatchesForIngredientFEFO(ingredient.getId());
                    BigDecimal remainingToDeduct = totalDeductBaseQty;
                    for (PurchaseItem batch : batches) {
                        if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                        BigDecimal batchQty = batch.getRemainingQuantity();
                        if (batchQty.compareTo(BigDecimal.ZERO) <= 0) continue;

                        BigDecimal deduct = batchQty.min(remainingToDeduct);
                        batch.setRemainingQuantity(batchQty.subtract(deduct).setScale(4, RoundingMode.HALF_UP));
                        purchaseItemRepository.save(batch);
                        remainingToDeduct = remainingToDeduct.subtract(deduct);
                    }

                    // Update ingredient expiry_date to the earliest expiry date of remaining active batches
                    LocalDate nextExpiry = batches.stream()
                            .filter(b -> b.getRemainingQuantity() != null && b.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0 && b.getExpiryDate() != null)
                            .map(PurchaseItem::getExpiryDate)
                            .min(LocalDate::compareTo)
                            .orElse(null);
                    if (nextExpiry != null) {
                        ingredient.setExpiryDate(nextExpiry);
                    }

                    ingredientRepository.save(ingredient);

                    // Record stock movement: SALE
                    recordLog(ingredient.getId(), null, StockMovementType.SALE.name(), totalDeductBaseQty, oldStock, newStock,
                            "Order #" + order.getOrderNumber() + " (" + product.getName() + " x" + orderQty + ") [FEFO]", currentUserId);
                }
            } else {
                // Direct finished product stock deduction using FEFO
                if (product.getStock() < orderQty) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for product " + product.getName() +
                            ". Available: " + product.getStock() + ", Required: " + orderQty);
                }
                int oldStock = product.getStock();
                int newStock = oldStock - orderQty;
                product.setStock(newStock);

                // FEFO deduction from product batches
                List<PurchaseItem> productBatches = purchaseItemRepository.findActiveBatchesForProductFEFO(product.getId());
                BigDecimal prodRemainingToDeduct = new BigDecimal(orderQty);
                for (PurchaseItem batch : productBatches) {
                    if (prodRemainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;
                    BigDecimal batchQty = batch.getRemainingQuantity();
                    if (batchQty.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal deduct = batchQty.min(prodRemainingToDeduct);
                    batch.setRemainingQuantity(batchQty.subtract(deduct).setScale(4, RoundingMode.HALF_UP));
                    purchaseItemRepository.save(batch);
                    prodRemainingToDeduct = prodRemainingToDeduct.subtract(deduct);
                }

                // Update product expiry_date to next active batch
                LocalDate nextProdExpiry = productBatches.stream()
                        .filter(b -> b.getRemainingQuantity() != null && b.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0 && b.getExpiryDate() != null)
                        .map(PurchaseItem::getExpiryDate)
                        .min(LocalDate::compareTo)
                        .orElse(null);
                if (nextProdExpiry != null) {
                    product.setExpiryDate(nextProdExpiry);
                }

                productRepository.save(product);

                recordLog(null, product.getId(), StockMovementType.SALE.name(), new BigDecimal(orderQty),
                        new BigDecimal(oldStock), new BigDecimal(newStock),
                        "Order #" + order.getOrderNumber() + " sale [FEFO]", currentUserId);
            }
        }
    }

    @Override
    @Transactional
    public Ingredient processPurchaseIntake(Integer ingredientId, Integer purchaseUnitId, BigDecimal purchaseQty,
                                            BigDecimal totalPurchaseCost, String note, Integer currentUserId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        if (purchaseQty == null || purchaseQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase quantity must be greater than zero");
        }
        if (totalPurchaseCost == null || totalPurchaseCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total purchase cost must be non-negative");
        }

        BigDecimal incomingBaseQty = purchaseQty;
        if (purchaseUnitId != null) {
            IngredientPurchaseUnit pu = purchaseUnitRepository.findById(purchaseUnitId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase unit not found"));
            incomingBaseQty = unitConversionService.convertPurchaseUnitToBase(purchaseQty, pu);
        }

        BigDecimal costPerBaseUnit = totalPurchaseCost.divide(incomingBaseQty, 6, RoundingMode.HALF_UP);

        BigDecimal currentStock = ingredient.getStock() != null ? ingredient.getStock() : BigDecimal.ZERO;
        BigDecimal currentCost = ingredient.getCost() != null ? ingredient.getCost() : BigDecimal.ZERO;

        BigDecimal newWeightedCost;
        BigDecimal newStock = currentStock.add(incomingBaseQty).setScale(4, RoundingMode.HALF_UP);

        if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            newWeightedCost = costPerBaseUnit;
        } else {
            BigDecimal currentValue = currentStock.multiply(currentCost);
            BigDecimal incomingValue = incomingBaseQty.multiply(costPerBaseUnit);
            BigDecimal totalValue = currentValue.add(incomingValue);
            newWeightedCost = totalValue.divide(newStock, 6, RoundingMode.HALF_UP);
        }

        ingredient.setCost(newWeightedCost);
        ingredient.setStock(newStock);
        Ingredient saved = ingredientRepository.save(ingredient);

        recordLog(ingredient.getId(), null, StockMovementType.PURCHASE.name(), incomingBaseQty, currentStock, newStock,
                note != null ? note : "Stock intake from purchase", currentUserId);

        recipeService.recalculateAllProductsUsingIngredient(ingredient.getId());

        log.info("Purchase intake for ingredient '{}': Added {} {} - New Stock: {} {}, Weighted Avg Cost: ${}/{}",
                ingredient.getName(), incomingBaseQty, ingredient.getUnit(), newStock, ingredient.getUnit(),
                newWeightedCost, ingredient.getUnit());

        return saved;
    }

    @Override
    @Transactional
    public Ingredient recordWaste(Integer ingredientId, BigDecimal quantity, String unit, String reason, Integer currentUserId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Waste quantity must be greater than zero");
        }

        BigDecimal baseQty = unitConversionService.convertToBase(quantity, unit, ingredient.getUnit());
        BigDecimal currentStock = ingredient.getStock() != null ? ingredient.getStock() : BigDecimal.ZERO;

        if (currentStock.compareTo(baseQty) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot record waste exceeding current stock. Stock: " + currentStock + " " + ingredient.getUnit());
        }

        BigDecimal newStock = currentStock.subtract(baseQty).setScale(4, RoundingMode.HALF_UP);
        ingredient.setStock(newStock);
        Ingredient saved = ingredientRepository.save(ingredient);

        recordLog(ingredient.getId(), null, StockMovementType.WASTE.name(), baseQty, currentStock, newStock,
                "Waste recorded: " + (reason != null ? reason : "Damaged/Expired"), currentUserId);

        return saved;
    }

    @Override
    @Transactional
    public Ingredient adjustStock(Integer ingredientId, BigDecimal actualQuantity, String unit, String note, Integer currentUserId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Actual stock cannot be negative");
        }

        BigDecimal actualBaseQty = unitConversionService.convertToBase(actualQuantity, unit, ingredient.getUnit());
        BigDecimal oldStock = ingredient.getStock() != null ? ingredient.getStock() : BigDecimal.ZERO;

        ingredient.setStock(actualBaseQty);
        Ingredient saved = ingredientRepository.save(ingredient);

        BigDecimal diff = actualBaseQty.subtract(oldStock).abs();
        recordLog(ingredient.getId(), null, StockMovementType.ADJUSTMENT.name(), diff, oldStock, actualBaseQty,
                note != null ? note : "Inventory physical count adjustment", currentUserId);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> getLowStockIngredients() {
        return ingredientRepository.findAll().stream()
                .filter(i -> i.getStock() != null && i.getMinStock() != null && i.getStock().compareTo(i.getMinStock()) <= 0)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryLog> getMovements(StockMovementType type) {
        List<InventoryLog> all = inventoryLogRepository.findAllByOrderByCreatedAtDesc();
        if (type != null) {
            all = all.stream().filter(l -> l.getType() != null && l.getType().equalsIgnoreCase(type.name())).toList();
        }
        return all;
    }

    private void recordLog(Integer ingredientId, Integer productId, String type, BigDecimal qty,
                           BigDecimal before, BigDecimal after, String note, Integer userId) {
        InventoryLog log = new InventoryLog();
        log.setIngredientId(ingredientId);
        log.setProductId(productId);
        log.setType(type);
        log.setQuantity(qty);
        log.setBeforeQty(before);
        log.setAfterQty(after);
        log.setNote(note);
        log.setUserId(userId);
        inventoryLogRepository.save(log);
    }
}

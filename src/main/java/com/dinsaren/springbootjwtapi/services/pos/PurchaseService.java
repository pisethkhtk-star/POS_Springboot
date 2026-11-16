package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.models.pos.Purchase;
import com.dinsaren.springbootjwtapi.models.pos.PurchaseItem;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseItemRequest;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final SupplierRepository supplierRepository;
    private final IngredientRepository ingredientRepository;
    private final PosProductRepository posProductRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final StockService stockService;

    public List<Purchase> getAllPurchases() {
        return purchaseRepository.findAllByOrderByCreatedAtDesc();
    }

    public Purchase getPurchaseById(Integer id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));
    }

    @Transactional
    public Purchase createPurchase(PurchaseCreateRequest request, Integer currentUserId) {
        if (request.getSupplierId() == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid purchase data: supplierId and items array are required");
        }

        supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier not found"));

        String reference = "PO-" + System.currentTimeMillis();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseItemRequest item : request.getItems()) {
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal cost = item.getCost() != null ? item.getCost() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(qty.multiply(cost));
        }
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        Purchase purchase = new Purchase();
        purchase.setReference(reference);
        purchase.setSupplierId(request.getSupplierId());
        purchase.setUserId(currentUserId);
        purchase.setTotalAmount(totalAmount);
        purchase.setNote(request.getNote());

        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (PurchaseItemRequest itemReq : request.getItems()) {
            BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal cost = itemReq.getCost() != null ? itemReq.getCost().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal subtotal = qty.multiply(cost).setScale(2, RoundingMode.HALF_UP);

            if (itemReq.getIngredientId() != null) {
                // 1. Purchase Ingredient
                Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient not found: " + itemReq.getIngredientId()));

                PurchaseItem purchaseItem = new PurchaseItem(savedPurchase, ingredient, qty, cost, subtotal);
                purchaseItemRepository.save(purchaseItem);
                savedPurchase.getItems().add(purchaseItem);

                // Increment ingredient stock & apply Weighted Average Cost
                stockService.processPurchaseIntake(ingredient.getId(), null, qty, subtotal, "Purchased via Ref #" + reference, currentUserId);

            } else if (itemReq.getProductId() != null) {
                // 2. Purchase Direct Product (products without recipe or retail items)
                PosProduct product = posProductRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + itemReq.getProductId()));

                PurchaseItem purchaseItem = new PurchaseItem(savedPurchase, product, qty, cost, subtotal);
                purchaseItemRepository.save(purchaseItem);
                savedPurchase.getItems().add(purchaseItem);

                // Increment direct product stock and calculate weighted average cost
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                int incomingQty = qty.intValue();
                int newStock = currentStock + incomingQty;

                BigDecimal currentCost = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
                BigDecimal newCost = currentCost;
                if (newStock > 0) {
                    BigDecimal currentTotalVal = currentCost.multiply(new BigDecimal(currentStock));
                    BigDecimal incomingVal = subtotal;
                    newCost = currentTotalVal.add(incomingVal).divide(new BigDecimal(newStock), 4, RoundingMode.HALF_UP);
                }

                product.setStock(newStock);
                product.setCost(newCost);
                posProductRepository.save(product);

                // Log movement to inventory_logs
                InventoryLog log = new InventoryLog();
                log.setProductId(product.getId());
                log.setType("IN");
                log.setQuantity(new BigDecimal(incomingQty));
                log.setBeforeQty(new BigDecimal(currentStock));
                log.setAfterQty(new BigDecimal(newStock));
                log.setNote("Product purchased via Ref #" + reference);
                log.setUserId(currentUserId);
                inventoryLogRepository.save(log);

            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each purchase item must specify either ingredientId or productId");
            }
        }

        return savedPurchase;
    }
}

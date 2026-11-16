package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
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
    private final InventoryLogRepository inventoryLogRepository;

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
            Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient not found: " + itemReq.getIngredientId()));

            BigDecimal qty = itemReq.getQuantity().setScale(2, RoundingMode.HALF_UP);
            BigDecimal cost = itemReq.getCost().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = qty.multiply(cost).setScale(2, RoundingMode.HALF_UP);

            PurchaseItem purchaseItem = new PurchaseItem(savedPurchase, ingredient, qty, cost, subtotal);
            purchaseItemRepository.save(purchaseItem);
            savedPurchase.getItems().add(purchaseItem);

            // Increment ingredient stock
            BigDecimal oldStock = ingredient.getStock();
            BigDecimal newStock = oldStock.add(qty).setScale(2, RoundingMode.HALF_UP);
            ingredient.setStock(newStock);
            ingredientRepository.save(ingredient);

            InventoryLog log = new InventoryLog();
            log.setIngredientId(ingredient.getId());
            log.setType("PURCHASE_IN");
            log.setQuantity(qty);
            log.setBeforeQty(oldStock);
            log.setAfterQty(newStock);
            log.setNote(request.getNote() != null ? request.getNote() : "Purchased via Ref #" + reference);
            log.setUserId(currentUserId);
            inventoryLogRepository.save(log);
        }

        return savedPurchase;
    }
}

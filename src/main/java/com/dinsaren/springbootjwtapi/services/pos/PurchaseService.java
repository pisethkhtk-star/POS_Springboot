package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.models.pos.Purchase;
import com.dinsaren.springbootjwtapi.models.pos.PurchaseItem;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseItemRequest;
import com.dinsaren.springbootjwtapi.payload.pos.PurchaseReceiveRequest;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public List<Purchase> getPurchasesByStatus(String status) {
        return purchaseRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
    }

    public Purchase getPurchaseById(Integer id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));
    }

    /**
     * Create a Purchase Order.
     * Note: Stock is NOT increased yet. The order is created in PENDING status.
     * Stock intake occurs only after the order is officially received via receivePurchase().
     */
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
        
        String initialStatus = "PENDING";
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            initialStatus = request.getStatus().trim().toUpperCase();
        }
        purchase.setStatus(initialStatus);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (PurchaseItemRequest itemReq : request.getItems()) {
            BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal cost = itemReq.getCost() != null ? itemReq.getCost().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal subtotal = qty.multiply(cost).setScale(2, RoundingMode.HALF_UP);
            LocalDate expiryDate = itemReq.getExpiryDate();

            if (itemReq.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findById(itemReq.getIngredientId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient not found: " + itemReq.getIngredientId()));

                PurchaseItem purchaseItem = new PurchaseItem(savedPurchase, ingredient, qty, cost, subtotal, expiryDate);
                purchaseItemRepository.save(purchaseItem);
                savedPurchase.getItems().add(purchaseItem);

            } else if (itemReq.getProductId() != null) {
                PosProduct product = posProductRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + itemReq.getProductId()));

                PurchaseItem purchaseItem = new PurchaseItem(savedPurchase, product, qty, cost, subtotal, expiryDate);
                purchaseItemRepository.save(purchaseItem);
                savedPurchase.getItems().add(purchaseItem);

            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each purchase item must specify either ingredientId or productId");
            }
        }

        log.info("Purchase Order created: Ref {} with {} item(s) in PENDING status", reference, savedPurchase.getItems().size());
        return savedPurchase;
    }

    /**
     * Receive goods from a Purchase Order into inventory stock.
     * Updates product / ingredient stock, applies Weighted Average Cost,
     * updates expiration date, records InventoryLog, and changes status to RECEIVED.
     */
    @Transactional
    public Purchase receivePurchase(Integer purchaseId, PurchaseReceiveRequest receiveRequest, Integer currentUserId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        if ("RECEIVED".equalsIgnoreCase(purchase.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This purchase order has already been received into stock");
        }
        if ("CANCELLED".equalsIgnoreCase(purchase.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot receive a cancelled purchase order");
        }

        // Optional: Update item expiry dates if provided in receiveRequest
        if (receiveRequest != null && receiveRequest.getItems() != null) {
            for (PurchaseReceiveRequest.PurchaseReceiveItemRequest itemUpdate : receiveRequest.getItems()) {
                if (itemUpdate.getItemId() != null && itemUpdate.getExpiryDate() != null) {
                    for (PurchaseItem pi : purchase.getItems()) {
                        if (pi.getId().equals(itemUpdate.getItemId())) {
                            pi.setExpiryDate(itemUpdate.getExpiryDate());
                            purchaseItemRepository.save(pi);
                        }
                    }
                }
            }
        }

        // Process stock intake for each purchase item
        for (PurchaseItem item : purchase.getItems()) {
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal cost = item.getCost() != null ? item.getCost() : BigDecimal.ZERO;
            BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : qty.multiply(cost).setScale(2, RoundingMode.HALF_UP);
            LocalDate expiryDate = item.getExpiryDate();

            if (item.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient not found: " + item.getIngredientId()));

                String logNote = "Received via Ref #" + purchase.getReference();
                if (expiryDate != null) {
                    logNote += " (Exp: " + expiryDate + ")";
                }

                // Increment ingredient stock & apply Weighted Average Cost
                stockService.processPurchaseIntake(ingredient.getId(), null, qty, subtotal, logNote, currentUserId);

                // Update ingredient expiration date if specified
                if (expiryDate != null) {
                    ingredient.setExpiryDate(expiryDate);
                    ingredientRepository.save(ingredient);
                }

            } else if (item.getProductId() != null) {
                PosProduct product = posProductRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found: " + item.getProductId()));

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
                if (expiryDate != null) {
                    product.setExpiryDate(expiryDate);
                }
                posProductRepository.save(product);

                // Log movement to inventory_logs (type: IN)
                InventoryLog invLog = new InventoryLog();
                invLog.setProductId(product.getId());
                invLog.setType("IN");
                invLog.setQuantity(new BigDecimal(incomingQty));
                invLog.setBeforeQty(new BigDecimal(currentStock));
                invLog.setAfterQty(new BigDecimal(newStock));

                String logNote = "Product received via Ref #" + purchase.getReference();
                if (expiryDate != null) {
                    logNote += " (Exp: " + expiryDate + ")";
                }
                invLog.setNote(logNote);
                invLog.setUserId(currentUserId);
                inventoryLogRepository.save(invLog);
            }
        }

        purchase.setStatus("RECEIVED");
        purchase.setReceivedAt(LocalDateTime.now());
        purchase.setReceivedById(currentUserId);
        if (receiveRequest != null && receiveRequest.getNote() != null && !receiveRequest.getNote().isBlank()) {
            purchase.setNote((purchase.getNote() != null ? purchase.getNote() + " | " : "") + receiveRequest.getNote());
        }

        Purchase saved = purchaseRepository.save(purchase);
        log.info("Purchase Order Ref {} officially RECEIVED into stock by user {}", purchase.getReference(), currentUserId);
        return saved;
    }

    /**
     * Cancel a pending purchase order.
     */
    @Transactional
    public Purchase cancelPurchase(Integer purchaseId, Integer currentUserId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        if ("RECEIVED".equalsIgnoreCase(purchase.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a purchase that has already been received into stock");
        }

        purchase.setStatus("CANCELLED");
        log.info("Purchase Order Ref {} CANCELLED by user {}", purchase.getReference(), currentUserId);
        return purchaseRepository.save(purchase);
    }
}

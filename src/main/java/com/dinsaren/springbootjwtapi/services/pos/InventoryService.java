package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.models.pos.PosProduct;
import com.dinsaren.springbootjwtapi.payload.pos.InventoryAdjustRequest;
import com.dinsaren.springbootjwtapi.repository.pos.IngredientRepository;
import com.dinsaren.springbootjwtapi.repository.pos.InventoryLogRepository;
import com.dinsaren.springbootjwtapi.repository.pos.PosProductRepository;
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
public class InventoryService {

    private final InventoryLogRepository inventoryLogRepository;
    private final PosProductRepository posProductRepository;
    private final IngredientRepository ingredientRepository;

    public List<InventoryLog> getAllLogs() {
        return inventoryLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Object adjustStock(InventoryAdjustRequest request, Integer currentUserId, String currentUserName) {
        if ((request.getProductId() == null && request.getIngredientId() == null) ||
            request.getType() == null || request.getQuantity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields: productId or ingredientId, type, and quantity are required");
        }

        String type = request.getType().toUpperCase();
        if (!List.of("IN", "OUT", "ADJUST", "ADJUSTMENT", "PURCHASE_IN", "ORDER_OUT").contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid adjustment type");
        }

        BigDecimal qty = request.getQuantity();
        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }

        if (request.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

            BigDecimal oldStock = ingredient.getStock();
            BigDecimal newStock = oldStock;

            if (type.equals("IN") || type.equals("PURCHASE_IN")) {
                newStock = oldStock.add(qty);
            } else if (type.equals("OUT") || type.equals("ORDER_OUT") || type.equals("ADJUST") || type.equals("ADJUSTMENT")) {
                newStock = oldStock.subtract(qty);
            }

            if (newStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient ingredient stock. Current stock: " + oldStock + " " + ingredient.getUnit() + ", cannot deduct " + qty);
            }

            newStock = newStock.setScale(2, RoundingMode.HALF_UP);
            ingredient.setStock(newStock);
            Ingredient updated = ingredientRepository.save(ingredient);

            InventoryLog log = new InventoryLog();
            log.setIngredientId(ingredient.getId());
            log.setType(type.equals("ADJUST") ? "ADJUSTMENT" : type);
            log.setQuantity(qty);
            log.setBeforeQty(oldStock);
            log.setAfterQty(newStock);
            log.setNote(request.getNote() != null ? request.getNote() : "Manual ingredient stock adjustment (" + type + ")");
            log.setUserId(currentUserId);
            inventoryLogRepository.save(log);

            return updated;
        }

        if (request.getProductId() != null) {
            PosProduct product = posProductRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            int oldStock = product.getStock();
            int intQty = qty.intValue();
            int newStock = oldStock;

            if (type.equals("IN") || type.equals("PURCHASE_IN")) {
                newStock = oldStock + intQty;
            } else if (type.equals("OUT") || type.equals("ORDER_OUT") || type.equals("ADJUST") || type.equals("ADJUSTMENT")) {
                newStock = oldStock - intQty;
            }

            if (newStock < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock. Current stock is " + oldStock + ", cannot deduct " + intQty);
            }

            product.setStock(newStock);
            PosProduct updated = posProductRepository.save(product);

            InventoryLog log = new InventoryLog();
            log.setProductId(product.getId());
            log.setType(type);
            log.setQuantity(qty);
            log.setBeforeQty(new BigDecimal(oldStock));
            log.setAfterQty(new BigDecimal(newStock));
            log.setNote(request.getNote() != null ? request.getNote() : "Manual stock adjustment (" + type + ")");
            log.setUserId(currentUserId);
            inventoryLogRepository.save(log);

            return updated;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neither productId nor ingredientId provided");
    }
}

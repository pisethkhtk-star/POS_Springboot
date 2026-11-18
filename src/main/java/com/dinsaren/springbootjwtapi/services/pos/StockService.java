package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import com.dinsaren.springbootjwtapi.models.pos.StockMovementType;

import java.math.BigDecimal;
import java.util.List;

public interface StockService {
    void deductStockForOrder(PosOrder order, Integer currentUserId);
    Ingredient processPurchaseIntake(Integer ingredientId, Integer purchaseUnitId, BigDecimal purchaseQty,
                                    BigDecimal totalPurchaseCost, String note, Integer currentUserId);
    Ingredient recordWaste(Integer ingredientId, BigDecimal quantity, String unit, String reason, Integer currentUserId);
    Ingredient adjustStock(Integer ingredientId, BigDecimal actualQuantity, String unit, String note, Integer currentUserId);
    List<Ingredient> getLowStockIngredients();
    List<InventoryLog> getMovements(StockMovementType type);
}

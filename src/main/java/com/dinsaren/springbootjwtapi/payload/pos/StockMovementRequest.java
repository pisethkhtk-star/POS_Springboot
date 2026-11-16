package com.dinsaren.springbootjwtapi.payload.pos;

import com.dinsaren.springbootjwtapi.models.pos.StockMovementType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockMovementRequest {
    private Integer ingredientId;
    private StockMovementType type; // PURCHASE, SALE, WASTE, ADJUSTMENT
    private BigDecimal quantity;
    private String unit; // If provided, converted to ingredient's base unit
    private String note;
    private String reason;
}

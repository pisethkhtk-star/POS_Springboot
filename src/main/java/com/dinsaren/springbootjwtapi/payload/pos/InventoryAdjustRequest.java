package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryAdjustRequest {
    private Integer productId;
    private Integer ingredientId;
    private String type;
    private BigDecimal quantity;
    private String note;
}

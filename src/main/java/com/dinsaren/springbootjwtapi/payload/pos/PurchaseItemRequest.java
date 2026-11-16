package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseItemRequest {
    private Integer ingredientId;
    private BigDecimal quantity;
    private BigDecimal cost;
}

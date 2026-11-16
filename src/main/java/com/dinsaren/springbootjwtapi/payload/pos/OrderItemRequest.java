package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Integer productId;
    private Integer quantity;
    private BigDecimal price;
}

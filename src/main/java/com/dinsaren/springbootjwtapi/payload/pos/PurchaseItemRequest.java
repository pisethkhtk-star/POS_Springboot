package com.dinsaren.springbootjwtapi.payload.pos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseItemRequest {
    private Integer ingredientId;
    private Integer productId;
    private BigDecimal quantity;
    private BigDecimal cost;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}

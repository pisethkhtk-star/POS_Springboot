package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DiscountRequest {
    private String name;
    private String type;
    private BigDecimal value;
    private BigDecimal minPurchase;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}

package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class IngredientRequest {
    private String name;
    private String code;
    private String unit;
    private BigDecimal stock;
    private BigDecimal minStock;
    private BigDecimal cost;
}

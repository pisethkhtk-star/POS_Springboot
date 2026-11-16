package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IngredientPurchaseUnitRequest {
    private String unitName;
    private String unitNameKm;
    private BigDecimal factorToBase;
    private BigDecimal defaultCost;
}

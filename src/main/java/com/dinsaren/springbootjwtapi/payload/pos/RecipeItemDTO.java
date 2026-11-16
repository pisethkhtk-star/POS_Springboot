package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeItemDTO {
    private Integer ingredientId;
    private String ingredientName;
    private String ingredientCode;
    private BigDecimal recipeQuantity;
    private String recipeUnit;
    private BigDecimal baseQuantity;
    private String baseUnit;
    private BigDecimal unitCost; // cost per base unit
    private BigDecimal subtotalCost; // baseQuantity * unitCost
}

package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeItemRequest {
    private Integer ingredientId;
    private BigDecimal quantity;
    private String unit;
}

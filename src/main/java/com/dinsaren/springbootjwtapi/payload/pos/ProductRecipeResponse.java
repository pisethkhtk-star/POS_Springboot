package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRecipeResponse {
    private Integer productId;
    private String productName;
    private String productCode;
    private BigDecimal productPrice;
    private BigDecimal cogs; // Calculated Cost of Goods Sold from recipe
    private List<RecipeItemDTO> items;
}

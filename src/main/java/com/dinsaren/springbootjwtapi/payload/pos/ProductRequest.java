package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String code;
    private String sku;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private Integer minStock;
    private Boolean hasRecipe;
    private String image;
    private Integer categoryId;
    private List<ProductIngredientDTO> ingredients;
}

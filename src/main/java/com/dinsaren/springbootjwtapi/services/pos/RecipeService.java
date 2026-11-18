package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.payload.pos.ProductRecipeResponse;
import com.dinsaren.springbootjwtapi.payload.pos.RecipeItemRequest;

import java.math.BigDecimal;
import java.util.List;

public interface RecipeService {
    ProductRecipeResponse getProductRecipe(Integer productId);
    ProductRecipeResponse saveRecipe(Integer productId, List<RecipeItemRequest> items);
    BigDecimal calculateProductCOGS(Integer productId);
    void recalculateAllProductsUsingIngredient(Integer ingredientId);
}

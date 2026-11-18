package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientPurchaseUnitRequest;

import java.util.List;

public interface IngredientPurchaseUnitService {
    List<IngredientPurchaseUnit> getUnits(Integer ingredientId);
    IngredientPurchaseUnit addUnit(Integer ingredientId, IngredientPurchaseUnitRequest request);
    void deleteUnit(Integer ingredientId, Integer unitId);
}

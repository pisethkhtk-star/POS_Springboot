package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.Ingredient;
import com.dinsaren.springbootjwtapi.payload.pos.IngredientRequest;

import java.util.List;

public interface IngredientService {
    List<Ingredient> searchIngredients(String query, Boolean lowStock);
    Ingredient getIngredientById(Integer id);
    Ingredient createIngredient(IngredientRequest request, Integer currentUserId, String currentUserName);
    Ingredient updateIngredient(Integer id, IngredientRequest request, Integer currentUserId, String currentUserName);
    void deleteIngredient(Integer id);
}

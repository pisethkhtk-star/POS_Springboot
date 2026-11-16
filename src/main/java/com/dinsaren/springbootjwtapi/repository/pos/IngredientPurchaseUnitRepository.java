package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.IngredientPurchaseUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientPurchaseUnitRepository extends JpaRepository<IngredientPurchaseUnit, Integer> {
    List<IngredientPurchaseUnit> findByIngredientId(Integer ingredientId);
    Optional<IngredientPurchaseUnit> findByIngredientIdAndUnitNameIgnoreCase(Integer ingredientId, String unitName);
    boolean existsByIngredientIdAndUnitNameIgnoreCase(Integer ingredientId, String unitName);
}

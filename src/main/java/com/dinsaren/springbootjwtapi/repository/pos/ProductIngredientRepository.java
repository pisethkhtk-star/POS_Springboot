package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Integer> {
    List<ProductIngredient> findByProductId(Integer productId);
    void deleteByProductId(Integer productId);
    long countByIngredientId(Integer ingredientId);
}

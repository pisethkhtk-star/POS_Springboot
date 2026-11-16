package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Integer> {
    List<PurchaseItem> findByPurchaseId(Integer purchaseId);
    long countByIngredientId(Integer ingredientId);
}

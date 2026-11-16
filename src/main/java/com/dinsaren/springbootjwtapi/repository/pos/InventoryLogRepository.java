package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {
    List<InventoryLog> findAllByOrderByCreatedAtDesc();
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(Integer productId);
    List<InventoryLog> findByIngredientIdOrderByCreatedAtDesc(Integer ingredientId);
    void deleteByProductId(Integer productId);
    void deleteByIngredientId(Integer ingredientId);
}

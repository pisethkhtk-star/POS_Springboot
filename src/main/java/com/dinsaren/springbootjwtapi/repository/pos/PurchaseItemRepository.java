package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Integer> {
    List<PurchaseItem> findByPurchaseId(Integer purchaseId);
    long countByIngredientId(Integer ingredientId);

    @Query("SELECT pi FROM PurchaseItem pi WHERE pi.ingredientId = :ingredientId " +
           "AND (pi.purchase.status = 'RECEIVED' OR pi.purchase.status IS NULL) " +
           "AND (pi.remainingQuantity IS NULL OR pi.remainingQuantity > 0) " +
           "ORDER BY pi.expiryDate ASC NULLS LAST, pi.id ASC")
    List<PurchaseItem> findActiveBatchesForIngredientFEFO(@Param("ingredientId") Integer ingredientId);

    @Query("SELECT pi FROM PurchaseItem pi WHERE pi.productId = :productId " +
           "AND (pi.purchase.status = 'RECEIVED' OR pi.purchase.status IS NULL) " +
           "AND (pi.remainingQuantity IS NULL OR pi.remainingQuantity > 0) " +
           "ORDER BY pi.expiryDate ASC NULLS LAST, pi.id ASC")
    List<PurchaseItem> findActiveBatchesForProductFEFO(@Param("productId") Integer productId);

    @Query("SELECT pi FROM PurchaseItem pi " +
           "JOIN FETCH pi.purchase p " +
           "LEFT JOIN FETCH pi.product " +
           "LEFT JOIN FETCH pi.ingredient " +
           "WHERE (p.status = 'RECEIVED' OR p.status IS NULL) " +
           "AND (pi.remainingQuantity IS NULL OR pi.remainingQuantity > 0) " +
           "ORDER BY pi.expiryDate ASC NULLS LAST, pi.id ASC")
    List<PurchaseItem> findActiveReceivedBatches();
}

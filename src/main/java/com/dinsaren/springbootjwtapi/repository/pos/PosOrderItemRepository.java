package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PosOrderItemRepository extends JpaRepository<PosOrderItem, Integer> {
    List<PosOrderItem> findByOrderId(Integer orderId);
    List<PosOrderItem> findByOrderIdIn(Collection<Integer> orderIds);
    long countByProductId(Integer productId);
}

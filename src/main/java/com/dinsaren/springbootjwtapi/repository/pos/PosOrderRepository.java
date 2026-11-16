package com.dinsaren.springbootjwtapi.repository.pos;

import com.dinsaren.springbootjwtapi.models.pos.OrderStatus;
import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosOrderRepository extends JpaRepository<PosOrder, Integer>, JpaSpecificationExecutor<PosOrder> {
    List<PosOrder> findAllByOrderByCreatedAtDesc();
    Optional<PosOrder> findByOrderNumber(String orderNumber);
    long countByCustomerId(Integer customerId);
    long countByUserId(Integer userId);
}

package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import com.dinsaren.springbootjwtapi.payload.pos.OrderCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.OrderPageResponse;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PosOrderService {

    Specification<PosOrder> buildOrderSpecification(String search, String statusStr, String startDateStr, String endDateStr);

    List<PosOrder> searchOrders(String search, String statusStr, String startDateStr, String endDateStr);

    OrderPageResponse searchOrdersPaged(String search, String statusStr, String startDateStr, String endDateStr, int page, int size);

    PosOrder getOrderById(Integer id);

    PosOrder createOrder(OrderCreateRequest request, Integer currentUserId);

    PosOrder updateOrderStatus(Integer id, String targetStatusStr, Integer currentUserId);
}

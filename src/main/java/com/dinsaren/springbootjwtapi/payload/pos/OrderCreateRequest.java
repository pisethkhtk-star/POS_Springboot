package com.dinsaren.springbootjwtapi.payload.pos;

import com.dinsaren.springbootjwtapi.models.pos.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateRequest {
    private Integer customerId;
    private List<OrderItemRequest> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal change;
    private String note;
}

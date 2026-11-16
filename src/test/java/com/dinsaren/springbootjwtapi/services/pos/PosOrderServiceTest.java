package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.OrderStatus;
import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import com.dinsaren.springbootjwtapi.payload.pos.OrderPageResponse;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PosOrderServiceTest {

    @Mock
    private PosOrderRepository posOrderRepository;

    @Mock
    private PosOrderItemRepository posOrderItemRepository;

    @Mock
    private PosProductRepository posProductRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private PosCustomerRepository posCustomerRepository;

    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private PosOrderService posOrderService;

    private List<PosOrder> sampleOrders;

    @BeforeEach
    void setUp() {
        sampleOrders = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            PosOrder order = new PosOrder();
            order.setId(i);
            order.setOrderNumber("POS-20261116-" + (1000 + i));
            order.setStatus(OrderStatus.COMPLETED);
            order.setTotal(new BigDecimal("10.00"));
            sampleOrders.add(order);
        }
    }

    @Test
    void testSearchOrdersPagedFirstPage() {
        // Page 1 with 20 items, total 45 items across 3 pages
        Page<PosOrder> page = new PageImpl<>(sampleOrders, Pageable.ofSize(20), 45);

        when(posOrderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(posOrderRepository.findAll(any(Specification.class))).thenReturn(sampleOrders);

        OrderPageResponse response = posOrderService.searchOrdersPaged(null, "COMPLETED", null, null, 1, 20);

        assertNotNull(response);
        assertEquals(20, response.getContent().size());
        assertEquals(1, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(45, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
        assertEquals(200.0, response.getTotalRevenue(), 0.01);
    }
}

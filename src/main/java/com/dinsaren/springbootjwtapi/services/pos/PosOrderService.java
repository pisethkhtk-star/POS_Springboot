package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.*;
import com.dinsaren.springbootjwtapi.payload.pos.OrderCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.OrderItemRequest;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PosOrderService {

    private final PosOrderRepository posOrderRepository;
    private final PosOrderItemRepository posOrderItemRepository;
    private final PosProductRepository posProductRepository;
    private final IngredientRepository ingredientRepository;
    private final PosCustomerRepository posCustomerRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final StockService stockService;

    public List<PosOrder> searchOrders(String search, String statusStr, String startDateStr, String endDateStr) {
        OrderStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusStr.trim())) {
            try {
                status = OrderStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        LocalDateTime startDate = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateStr.trim()).atStartOfDay();
            } catch (Exception ignored) {}
        }

        LocalDateTime endDate = null;
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateStr.trim()).atTime(23, 59, 59, 999000000);
            } catch (Exception ignored) {}
        }

        final OrderStatus finalStatus = status;
        final LocalDateTime finalStartDate = startDate;
        final LocalDateTime finalEndDate = endDate;
        final String finalSearch = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;

        Specification<PosOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (finalSearch != null) {
                Join<PosOrder, PosCustomer> customerJoin = root.join("customer", JoinType.LEFT);
                String pattern = "%" + finalSearch + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), pattern),
                    cb.like(cb.lower(customerJoin.get("name")), pattern)
                ));
            }

            if (finalStatus != null) {
                predicates.add(cb.equal(root.get("status"), finalStatus));
            }

            if (finalStartDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), finalStartDate));
            }

            if (finalEndDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), finalEndDate));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return posOrderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public PosOrder getOrderById(Integer id) {
        return posOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private String generateOrderNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = 1000 + new Random().nextInt(9000);
        return "POS-" + dateStr + "-" + rand;
    }

    @Transactional
    public PosOrder createOrder(OrderCreateRequest request, Integer currentUserId) {
        if (request.getItems() == null || request.getItems().isEmpty() || request.getPaymentMethod() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order details: items and paymentMethod are required");
        }

        String orderNumber = generateOrderNumber();

        PosOrder order = new PosOrder();
        order.setOrderNumber(orderNumber);
        order.setUserId(currentUserId);
        order.setCustomerId(request.getCustomerId());
        order.setSubtotal(request.getSubtotal());
        order.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        order.setTax(request.getTax() != null ? request.getTax() : BigDecimal.ZERO);
        order.setTotal(request.getTotal());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setAmountPaid(request.getAmountPaid());
        order.setChange(request.getChange() != null ? request.getChange() : BigDecimal.ZERO);
        order.setStatus(OrderStatus.COMPLETED);
        order.setNote(request.getNote());

        PosOrder savedOrder = posOrderRepository.save(order);

        for (OrderItemRequest itemReq : request.getItems()) {
            PosProduct product = posProductRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID " + itemReq.getProductId() + " not found"));

            int qty = itemReq.getQuantity();
            BigDecimal price = itemReq.getPrice();
            BigDecimal subtotal = price.multiply(new BigDecimal(qty));

            PosOrderItem orderItem = new PosOrderItem(savedOrder, product, qty, price, subtotal);
            posOrderItemRepository.save(orderItem);
            savedOrder.getItems().add(orderItem);
        }

        // Auto-deduct ingredient/product stock inside this single transaction
        stockService.deductStockForOrder(savedOrder, currentUserId);

        // Award loyalty points to customer (1 point per dollar spent)
        if (request.getCustomerId() != null) {
            posCustomerRepository.findById(request.getCustomerId()).ifPresent(c -> {
                int pointsToAdd = request.getTotal().intValue();
                c.setPoints(c.getPoints() + pointsToAdd);
                posCustomerRepository.save(c);
            });
        }

        return posOrderRepository.findById(savedOrder.getId()).orElse(savedOrder);
    }

    @Transactional
    public PosOrder updateOrderStatus(Integer id, String targetStatusStr, Integer currentUserId) {
        if (targetStatusStr == null || (!"REFUNDED".equalsIgnoreCase(targetStatusStr) 
                && !"CANCELLED".equalsIgnoreCase(targetStatusStr)
                && !"DELETED".equalsIgnoreCase(targetStatusStr))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target status: must be CANCELLED, REFUNDED, or DELETED");
        }

        OrderStatus targetStatus = OrderStatus.valueOf(targetStatusStr.toUpperCase());
        PosOrder order = getOrderById(id);

        if (order.getStatus() == OrderStatus.REFUNDED || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has already been cancelled, refunded, or deleted");
        }

        order.setStatus(targetStatus);
        PosOrder updatedOrder = posOrderRepository.save(order);

        // Revert stocks
        for (PosOrderItem item : order.getItems()) {
            PosProduct product = posProductRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                if (product.getHasRecipe() && product.getIngredients() != null && !product.getIngredients().isEmpty()) {
                    for (ProductIngredient pi : product.getIngredients()) {
                        Ingredient ing = pi.getIngredient();
                        BigDecimal returnedQty = pi.getQuantity().multiply(new BigDecimal(item.getQuantity()));
                        BigDecimal currentStock = ing.getStock();
                        BigDecimal newStock = currentStock.add(returnedQty).setScale(2, RoundingMode.HALF_UP);

                        ing.setStock(newStock);
                        ingredientRepository.save(ing);

                        InventoryLog log = new InventoryLog();
                        log.setIngredientId(ing.getId());
                        log.setType("IN");
                        log.setQuantity(returnedQty);
                        log.setBeforeQty(currentStock);
                        log.setAfterQty(newStock);
                        log.setNote("Order " + targetStatus + " restock return - Order #" + order.getOrderNumber() + " (" + product.getName() + " x" + item.getQuantity() + ")");
                        log.setUserId(currentUserId);
                        inventoryLogRepository.save(log);
                    }
                } else {
                    int oldStock = product.getStock();
                    int newStock = oldStock + item.getQuantity();
                    product.setStock(newStock);
                    posProductRepository.save(product);

                    InventoryLog log = new InventoryLog();
                    log.setProductId(product.getId());
                    log.setType("IN");
                    log.setQuantity(new BigDecimal(item.getQuantity()));
                    log.setBeforeQty(new BigDecimal(oldStock));
                    log.setAfterQty(new BigDecimal(newStock));
                    log.setNote("Order " + targetStatus + " restock return - Order #" + order.getOrderNumber());
                    log.setUserId(currentUserId);
                    inventoryLogRepository.save(log);
                }
            }
        }

        // Deduct points from customer
        if (order.getCustomerId() != null) {
            posCustomerRepository.findById(order.getCustomerId()).ifPresent(c -> {
                int pointsToDeduct = order.getTotal().intValue();
                c.setPoints(Math.max(0, c.getPoints() - pointsToDeduct));
                posCustomerRepository.save(c);
            });
        }

        return updatedOrder;
    }
}

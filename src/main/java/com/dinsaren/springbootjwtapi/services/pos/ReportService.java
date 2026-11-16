package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.pos.OrderStatus;
import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import com.dinsaren.springbootjwtapi.models.pos.PosOrderItem;
import com.dinsaren.springbootjwtapi.repository.pos.PosOrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PosOrderRepository posOrderRepository;

    public Map<String, Object> getReports(String startDateStr, String endDateStr) {
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

        final LocalDateTime finalStartDate = startDate;
        final LocalDateTime finalEndDate = endDate;

        Specification<PosOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), OrderStatus.COMPLETED));

            if (finalStartDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), finalStartDate));
            }
            if (finalEndDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), finalEndDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<PosOrder> orders = posOrderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        double totalRevenue = 0;
        double totalCostOfGoods = 0;
        double totalDiscount = 0;
        double totalTax = 0;
        int orderCount = orders.size();

        Map<String, double[]> salesByDateMap = new TreeMap<>();
        Map<String, double[]> salesByCategoryMap = new HashMap<>();
        Map<String, double[]> salesByCashierMap = new HashMap<>();
        Map<String, double[]> productSalesMap = new HashMap<>();
        Map<String, double[]> paymentMethodMap = new HashMap<>();

        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");

        for (PosOrder order : orders) {
            double rev = order.getTotal().doubleValue();
            double disc = order.getDiscount() != null ? order.getDiscount().doubleValue() : 0;
            double tx = order.getTax() != null ? order.getTax().doubleValue() : 0;

            totalRevenue += rev;
            totalDiscount += disc;
            totalTax += tx;

            // Payment method breakdown
            String payMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "CASH";
            paymentMethodMap.putIfAbsent(payMethod, new double[]{0, 0});
            double[] payVals = paymentMethodMap.get(payMethod);
            payVals[0] += rev;
            payVals[1] += 1;

            double orderCost = 0;
            if (order.getItems() != null) {
                for (PosOrderItem item : order.getItems()) {
                    int qty = item.getQuantity();
                    double unitCost = item.getProduct() != null && item.getProduct().getCost() != null ?
                            item.getProduct().getCost().doubleValue() : 0;
                    double itemCost = unitCost * qty;
                    orderCost += itemCost;

                    String prodName = item.getProduct() != null ? item.getProduct().getName() : "Unknown";
                    productSalesMap.putIfAbsent(prodName, new double[]{0, 0, 0});
                    double[] prodVals = productSalesMap.get(prodName);
                    prodVals[0] += qty;
                    prodVals[1] += item.getSubtotal().doubleValue();
                    prodVals[2] += itemCost;

                    String catName = (item.getProduct() != null && item.getProduct().getCategory() != null) ?
                            item.getProduct().getCategory().getName() : "Uncategorized";

                    salesByCategoryMap.putIfAbsent(catName, new double[]{0, 0});
                    double[] catVals = salesByCategoryMap.get(catName);
                    catVals[0] += item.getSubtotal().doubleValue();
                    catVals[1] += qty;
                }
            }
            totalCostOfGoods += orderCost;

            String dateKey = order.getCreatedAt() != null ? dateFmt.format(order.getCreatedAt()) : "Unknown";
            salesByDateMap.putIfAbsent(dateKey, new double[]{0, 0, 0});
            double[] dateVals = salesByDateMap.get(dateKey);
            dateVals[0] += rev;
            dateVals[1] += orderCost;
            dateVals[2] += 1;

            String cashierName = order.getUser() != null && order.getUser().getUsername() != null ?
                    order.getUser().getUsername() : "System";
            salesByCashierMap.putIfAbsent(cashierName, new double[]{0, 0});
            double[] cashierVals = salesByCashierMap.get(cashierName);
            cashierVals[0] += rev;
            cashierVals[1] += 1;
        }

        double netProfit = totalRevenue - totalCostOfGoods;
        double avgOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0;
        double profitMarginPct = totalRevenue > 0 ? (netProfit / totalRevenue) * 100.0 : 0.0;

        List<Map<String, Object>> salesByDate = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : salesByDateMap.entrySet()) {
            double r = entry.getValue()[0];
            double c = entry.getValue()[1];
            double cnt = entry.getValue()[2];
            salesByDate.add(Map.of(
                "date", entry.getKey(),
                "revenue", Math.round(r * 100.0) / 100.0,
                "cost", Math.round(c * 100.0) / 100.0,
                "profit", Math.round((r - c) * 100.0) / 100.0,
                "orders", (int) cnt
            ));
        }

        List<Map<String, Object>> salesByCategory = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : salesByCategoryMap.entrySet()) {
            salesByCategory.add(Map.of(
                "category", entry.getKey(),
                "revenue", Math.round(entry.getValue()[0] * 100.0) / 100.0,
                "quantity", (int) entry.getValue()[1]
            ));
        }

        List<Map<String, Object>> salesByCashier = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : salesByCashierMap.entrySet()) {
            salesByCashier.add(Map.of(
                "cashier", entry.getKey(),
                "revenue", Math.round(entry.getValue()[0] * 100.0) / 100.0,
                "orders", (int) entry.getValue()[1]
            ));
        }

        // Top Selling Products
        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : productSalesMap.entrySet()) {
            double q = entry.getValue()[0];
            double rev = entry.getValue()[1];
            double cst = entry.getValue()[2];
            double pft = rev - cst;
            double pftMargin = rev > 0 ? (pft / rev) * 100.0 : 0.0;
            topProducts.add(Map.of(
                "productName", entry.getKey(),
                "quantity", (int) q,
                "revenue", Math.round(rev * 100.0) / 100.0,
                "cost", Math.round(cst * 100.0) / 100.0,
                "profit", Math.round(pft * 100.0) / 100.0,
                "marginPct", Math.round(pftMargin * 10.0) / 10.0
            ));
        }
        topProducts.sort((a, b) -> Double.compare(
            ((Number) b.get("revenue")).doubleValue(), 
            ((Number) a.get("revenue")).doubleValue()
        ));

        // Payment Methods Breakdown
        List<Map<String, Object>> paymentMethods = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : paymentMethodMap.entrySet()) {
            double amount = entry.getValue()[0];
            double count = entry.getValue()[1];
            double share = totalRevenue > 0 ? (amount / totalRevenue) * 100.0 : 0.0;
            paymentMethods.add(Map.of(
                "method", entry.getKey(),
                "amount", Math.round(amount * 100.0) / 100.0,
                "count", (int) count,
                "sharePct", Math.round(share * 10.0) / 10.0
            ));
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        summary.put("totalRevenueKhr", Math.round(totalRevenue * 4100));
        summary.put("totalCostOfGoods", Math.round(totalCostOfGoods * 100.0) / 100.0);
        summary.put("totalCostOfGoodsKhr", Math.round(totalCostOfGoods * 4100));
        summary.put("totalDiscount", Math.round(totalDiscount * 100.0) / 100.0);
        summary.put("totalTax", Math.round(totalTax * 100.0) / 100.0);
        summary.put("netProfit", Math.round(netProfit * 100.0) / 100.0);
        summary.put("netProfitKhr", Math.round(netProfit * 4100));
        summary.put("profitMarginPct", Math.round(profitMarginPct * 10.0) / 10.0);
        summary.put("orderCount", orderCount);
        summary.put("avgOrderValue", Math.round(avgOrderValue * 100.0) / 100.0);
        summary.put("exchangeRate", 4100);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("salesByDate", salesByDate);
        response.put("salesByCategory", salesByCategory);
        response.put("salesByCashier", salesByCashier);
        response.put("topProducts", topProducts);
        response.put("paymentMethods", paymentMethods);

        return response;
    }
}

package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.models.pos.*;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import com.dinsaren.springbootjwtapi.services.pos.ReportService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PosOrderRepository posOrderRepository;
    private final PosOrderItemRepository posOrderItemRepository;
    private final PosProductRepository posProductRepository;
    private final IngredientRepository ingredientRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReports(String startDateStr, String endDateStr) {
        Date startDate = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(startDateStr.trim());
                startDate = Timestamp.valueOf(ld.atStartOfDay());
            } catch (Exception ignored) {}
        }

        Date endDate = null;
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(endDateStr.trim());
                endDate = Timestamp.valueOf(ld.atTime(23, 59, 59, 999000000));
            } catch (Exception ignored) {}
        }

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;

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

        List<PosOrder> orders = posOrderRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "createdAt"));

        double totalRevenue = 0.0;
        double totalDiscount = 0.0;
        double totalTax = 0.0;
        int orderCount = orders.size();

        Map<String, Double> salesByDateMap = new LinkedHashMap<>();
        Map<String, Double> costByDateMap = new LinkedHashMap<>();
        Map<String, Integer> ordersByDateMap = new LinkedHashMap<>();
        Map<String, Double> paymentMethodMap = new HashMap<>();
        Map<String, Double> salesByCashierMap = new HashMap<>();
        Map<String, Integer> ordersByCashierMap = new HashMap<>();

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (PosOrder o : orders) {
            double netTotal = o.getTotal() != null ? o.getTotal().doubleValue() : 0.0;
            double disc = o.getDiscount() != null ? o.getDiscount().doubleValue() : 0.0;
            double tax = o.getTax() != null ? o.getTax().doubleValue() : 0.0;

            totalRevenue += netTotal;
            totalDiscount += disc;
            totalTax += tax;

            String dateKey = o.getCreatedAt() != null ? dayFormat.format(o.getCreatedAt()) : "Unknown";
            salesByDateMap.put(dateKey, salesByDateMap.getOrDefault(dateKey, 0.0) + netTotal);
            ordersByDateMap.put(dateKey, ordersByDateMap.getOrDefault(dateKey, 0) + 1);

            String pm = o.getPaymentMethod() != null ? o.getPaymentMethod().name() : "OTHER";
            paymentMethodMap.put(pm, paymentMethodMap.getOrDefault(pm, 0.0) + netTotal);

            String cashierName = (o.getUser() != null && o.getUser().getName() != null)
                    ? o.getUser().getName()
                    : ((o.getUser() != null && o.getUser().getUsername() != null) ? o.getUser().getUsername() : "Admin User");
            salesByCashierMap.put(cashierName, salesByCashierMap.getOrDefault(cashierName, 0.0) + netTotal);
            ordersByCashierMap.put(cashierName, ordersByCashierMap.getOrDefault(cashierName, 0) + 1);
        }

        List<Integer> orderIds = orders.stream().map(PosOrder::getId).collect(Collectors.toList());
        List<PosOrderItem> allOrderItems = orderIds.isEmpty() ? Collections.emptyList() : posOrderItemRepository.findByOrderIdIn(orderIds);

        double totalCostOfGoods = 0.0;
        Map<Integer, Integer> productQtyMap = new HashMap<>();
        Map<Integer, Double> productRevenueMap = new HashMap<>();
        Map<Integer, Double> productCostMap = new HashMap<>();

        Map<String, Double> categoryRevenueMap = new HashMap<>();
        Map<String, Integer> categoryQtyMap = new HashMap<>();

        for (PosOrderItem item : allOrderItems) {
            PosProduct p = item.getProduct();
            if (p == null) continue;

            int qty = item.getQuantity();
            double unitPrice = item.getPrice() != null ? item.getPrice().doubleValue() : 0.0;
            double unitCost = p.getCost() != null ? p.getCost().doubleValue() : 0.0;

            double itemRev = unitPrice * qty;
            double itemCost = unitCost * qty;

            totalCostOfGoods += itemCost;

            productQtyMap.put(p.getId(), productQtyMap.getOrDefault(p.getId(), 0) + qty);
            productRevenueMap.put(p.getId(), productRevenueMap.getOrDefault(p.getId(), 0.0) + itemRev);
            productCostMap.put(p.getId(), productCostMap.getOrDefault(p.getId(), 0.0) + itemCost);

            String catName = (p.getCategory() != null && p.getCategory().getName() != null)
                    ? p.getCategory().getName()
                    : "Uncategorized";
            categoryRevenueMap.put(catName, categoryRevenueMap.getOrDefault(catName, 0.0) + itemRev);
            categoryQtyMap.put(catName, categoryQtyMap.getOrDefault(catName, 0) + qty);

            if (item.getOrder() != null && item.getOrder().getCreatedAt() != null) {
                String dKey = dayFormat.format(item.getOrder().getCreatedAt());
                costByDateMap.put(dKey, costByDateMap.getOrDefault(dKey, 0.0) + itemCost);
            }
        }

        double netProfit = totalRevenue - totalCostOfGoods;
        double profitMarginPct = totalRevenue > 0 ? (netProfit / totalRevenue) * 100.0 : 0.0;
        double avgOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

        List<Map<String, Object>> salesByDate = new ArrayList<>();
        for (String dateKey : salesByDateMap.keySet()) {
            double rev = salesByDateMap.getOrDefault(dateKey, 0.0);
            double cogs = costByDateMap.getOrDefault(dateKey, 0.0);
            double prof = rev - cogs;
            int count = ordersByDateMap.getOrDefault(dateKey, 0);

            Map<String, Object> point = new HashMap<>();
            point.put("date", dateKey);
            point.put("revenue", Math.round(rev * 100.0) / 100.0);
            point.put("cost", Math.round(cogs * 100.0) / 100.0);
            point.put("profit", Math.round(prof * 100.0) / 100.0);
            point.put("orders", count);
            salesByDate.add(point);
        }

        List<Map<String, Object>> salesByCategory = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryRevenueMap.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("category", entry.getKey());
            cat.put("revenue", Math.round(entry.getValue() * 100.0) / 100.0);
            cat.put("quantity", categoryQtyMap.getOrDefault(entry.getKey(), 0));
            double share = totalRevenue > 0 ? (entry.getValue() / totalRevenue) * 100.0 : 0.0;
            cat.put("percentage", Math.round(share * 10.0) / 10.0);
            salesByCategory.add(cat);
        }
        salesByCategory.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));

        List<Map<String, Object>> topProducts = new ArrayList<>();
        Map<Integer, PosProduct> productEntityMap = new HashMap<>();
        if (!productQtyMap.isEmpty()) {
            posProductRepository.findAllById(productQtyMap.keySet()).forEach(p -> productEntityMap.put(p.getId(), p));
        }

        for (Map.Entry<Integer, Integer> entry : productQtyMap.entrySet()) {
            Integer pId = entry.getKey();
            PosProduct p = productEntityMap.get(pId);
            if (p == null) continue;

            int qty = entry.getValue();
            double rev = productRevenueMap.getOrDefault(pId, 0.0);
            double cost = productCostMap.getOrDefault(pId, 0.0);
            double profit = rev - cost;
            double margin = rev > 0 ? (profit / rev) * 100.0 : 0.0;

            Map<String, Object> pMap = new HashMap<>();
            pMap.put("id", pId);
            pMap.put("name", p.getName());
            pMap.put("code", p.getCode());
            pMap.put("category", p.getCategory() != null ? p.getCategory().getName() : "—");
            pMap.put("quantity", qty);
            pMap.put("revenue", Math.round(rev * 100.0) / 100.0);
            pMap.put("cost", Math.round(cost * 100.0) / 100.0);
            pMap.put("profit", Math.round(profit * 100.0) / 100.0);
            pMap.put("margin", Math.round(margin * 10.0) / 10.0);
            topProducts.add(pMap);
        }
        topProducts.sort((a, b) -> Integer.compare((Integer) b.get("quantity"), (Integer) a.get("quantity")));

        List<Map<String, Object>> paymentMethods = new ArrayList<>();
        for (Map.Entry<String, Double> entry : paymentMethodMap.entrySet()) {
            Map<String, Object> pm = new HashMap<>();
            pm.put("method", entry.getKey());
            pm.put("amount", Math.round(entry.getValue() * 100.0) / 100.0);
            double pct = totalRevenue > 0 ? (entry.getValue() / totalRevenue) * 100.0 : 0.0;
            pm.put("percentage", Math.round(pct * 10.0) / 10.0);
            paymentMethods.add(pm);
        }
        paymentMethods.sort((a, b) -> Double.compare((Double) b.get("amount"), (Double) a.get("amount")));

        List<Map<String, Object>> salesByCashier = new ArrayList<>();
        for (Map.Entry<String, Double> entry : salesByCashierMap.entrySet()) {
            Map<String, Object> c = new HashMap<>();
            c.put("cashier", entry.getKey());
            c.put("revenue", Math.round(entry.getValue() * 100.0) / 100.0);
            c.put("orders", ordersByCashierMap.getOrDefault(entry.getKey(), 0));
            double avg = ordersByCashierMap.getOrDefault(entry.getKey(), 1) > 0
                    ? entry.getValue() / ordersByCashierMap.get(entry.getKey())
                    : 0.0;
            c.put("averageOrderValue", Math.round(avg * 100.0) / 100.0);
            salesByCashier.add(c);
        }
        salesByCashier.sort((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")));

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

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getExpiryReport(String search, String type, String status) {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> items = new ArrayList<>();

        // 1. Gather all active batches from received purchase items
        List<PurchaseItem> batches = purchaseItemRepository.findActiveReceivedBatches();
        Set<Integer> trackedProductIds = new HashSet<>();
        Set<Integer> trackedIngredientIds = new HashSet<>();

        for (PurchaseItem pi : batches) {
            LocalDate exp = pi.getExpiryDate();
            BigDecimal remaining = pi.getRemainingQuantity();
            if (remaining == null || remaining.compareTo(BigDecimal.ZERO) <= 0) continue;

            if (pi.getIngredient() != null) {
                Ingredient ing = pi.getIngredient();
                if (exp == null && ing.getExpiryDate() != null) exp = ing.getExpiryDate();
                trackedIngredientIds.add(ing.getId());
                long days = exp != null ? ChronoUnit.DAYS.between(today, exp) : 99999;
                String st = exp == null ? "VALID" : (days < 0 ? "EXPIRED" : (days <= 7 ? "EXPIRING_SOON" : "VALID"));

                Map<String, Object> row = new HashMap<>();
                row.put("id", "batch-ing-" + pi.getId());
                row.put("itemId", ing.getId());
                row.put("name", ing.getName());
                row.put("code", ing.getCode());
                row.put("type", "INGREDIENT");
                row.put("unit", ing.getUnit());
                row.put("stock", remaining);
                row.put("totalStock", ing.getStock());
                row.put("cost", pi.getCost() != null ? pi.getCost() : ing.getCost());
                row.put("expiryDate", exp != null ? exp.toString() : null);
                row.put("daysRemaining", exp != null ? days : null);
                row.put("status", st);
                row.put("batchRef", pi.getPurchase() != null ? pi.getPurchase().getReference() : "N/A");
                row.put("purchaseId", pi.getPurchase() != null ? pi.getPurchase().getId() : null);
                items.add(row);
            } else if (pi.getProduct() != null) {
                PosProduct prod = pi.getProduct();
                if (exp == null && prod.getExpiryDate() != null) exp = prod.getExpiryDate();
                trackedProductIds.add(prod.getId());
                long days = exp != null ? ChronoUnit.DAYS.between(today, exp) : 99999;
                String st = exp == null ? "VALID" : (days < 0 ? "EXPIRED" : (days <= 7 ? "EXPIRING_SOON" : "VALID"));

                Map<String, Object> row = new HashMap<>();
                row.put("id", "batch-prod-" + pi.getId());
                row.put("itemId", prod.getId());
                row.put("name", prod.getName());
                row.put("code", prod.getCode());
                row.put("type", "PRODUCT");
                row.put("unit", "pcs");
                row.put("stock", remaining);
                row.put("totalStock", prod.getStock());
                row.put("cost", pi.getCost() != null ? pi.getCost() : prod.getCost());
                row.put("expiryDate", exp != null ? exp.toString() : null);
                row.put("daysRemaining", exp != null ? days : null);
                row.put("status", st);
                row.put("batchRef", pi.getPurchase() != null ? pi.getPurchase().getReference() : "N/A");
                row.put("purchaseId", pi.getPurchase() != null ? pi.getPurchase().getId() : null);
                items.add(row);
            }
        }

        // 2. Also include all standalone products and ingredients currently in stock
        List<PosProduct> products = posProductRepository.findAll();
        for (PosProduct prod : products) {
            if (!trackedProductIds.contains(prod.getId()) && prod.getStock() != null && prod.getStock() > 0) {
                LocalDate exp = prod.getExpiryDate();
                long days = exp != null ? ChronoUnit.DAYS.between(today, exp) : 99999;
                String st = exp == null ? "VALID" : (days < 0 ? "EXPIRED" : (days <= 7 ? "EXPIRING_SOON" : "VALID"));

                Map<String, Object> row = new HashMap<>();
                row.put("id", "prod-" + prod.getId());
                row.put("itemId", prod.getId());
                row.put("name", prod.getName());
                row.put("code", prod.getCode());
                row.put("type", "PRODUCT");
                row.put("unit", "pcs");
                row.put("stock", prod.getStock());
                row.put("totalStock", prod.getStock());
                row.put("cost", prod.getCost());
                row.put("expiryDate", exp != null ? exp.toString() : null);
                row.put("daysRemaining", exp != null ? days : null);
                row.put("status", st);
                row.put("batchRef", "Initial Stock");
                row.put("purchaseId", null);
                items.add(row);
            }
        }

        List<Ingredient> ingredients = ingredientRepository.findAll();
        for (Ingredient ing : ingredients) {
            if (!trackedIngredientIds.contains(ing.getId()) && ing.getStock() != null && ing.getStock().compareTo(BigDecimal.ZERO) > 0) {
                LocalDate exp = ing.getExpiryDate();
                long days = exp != null ? ChronoUnit.DAYS.between(today, exp) : 99999;
                String st = exp == null ? "VALID" : (days < 0 ? "EXPIRED" : (days <= 7 ? "EXPIRING_SOON" : "VALID"));

                Map<String, Object> row = new HashMap<>();
                row.put("id", "ing-" + ing.getId());
                row.put("itemId", ing.getId());
                row.put("name", ing.getName());
                row.put("code", ing.getCode());
                row.put("type", "INGREDIENT");
                row.put("unit", ing.getUnit());
                row.put("stock", ing.getStock());
                row.put("totalStock", ing.getStock());
                row.put("cost", ing.getCost());
                row.put("expiryDate", exp != null ? exp.toString() : null);
                row.put("daysRemaining", exp != null ? days : null);
                row.put("status", st);
                row.put("batchRef", "Initial Stock");
                row.put("purchaseId", null);
                items.add(row);
            }
        }

        // 3. Filter by search, type, and status if requested
        String cleanSearch = search != null ? search.trim().toLowerCase() : null;
        String cleanType = type != null && !"ALL".equalsIgnoreCase(type.trim()) ? type.trim().toUpperCase() : null;
        String cleanStatus = status != null && !"ALL".equalsIgnoreCase(status.trim()) ? status.trim().toUpperCase() : null;

        List<Map<String, Object>> filtered = items.stream().filter(row -> {
            if (cleanSearch != null && !cleanSearch.isEmpty()) {
                String name = String.valueOf(row.get("name")).toLowerCase();
                String code = String.valueOf(row.get("code")).toLowerCase();
                String batchRef = String.valueOf(row.get("batchRef")).toLowerCase();
                if (!name.contains(cleanSearch) && !code.contains(cleanSearch) && !batchRef.contains(cleanSearch)) {
                    return false;
                }
            }
            if (cleanType != null && !cleanType.equals(row.get("type"))) {
                return false;
            }
            if (cleanStatus != null && !cleanStatus.equals(row.get("status"))) {
                return false;
            }
            return true;
        }).sorted((a, b) -> {
            String expA = (String) a.get("expiryDate");
            String expB = (String) b.get("expiryDate");
            if (expA == null && expB == null) return String.valueOf(a.get("name")).compareToIgnoreCase(String.valueOf(b.get("name")));
            if (expA == null) return 1;
            if (expB == null) return -1;
            int cmp = expA.compareTo(expB);
            if (cmp != 0) return cmp;
            return String.valueOf(a.get("name")).compareToIgnoreCase(String.valueOf(b.get("name")));
        }).collect(Collectors.toList());

        // 4. Compute Summary KPIs
        long expiredCount = items.stream().filter(r -> "EXPIRED".equals(r.get("status"))).count();
        long expiringSoonCount = items.stream().filter(r -> "EXPIRING_SOON".equals(r.get("status"))).count();
        long validCount = items.stream().filter(r -> "VALID".equals(r.get("status"))).count();

        double expiredValue = items.stream()
                .filter(r -> "EXPIRED".equals(r.get("status")))
                .mapToDouble(r -> {
                    Number stock = (Number) r.get("stock");
                    Number cost = (Number) r.get("cost");
                    return stock != null && cost != null ? stock.doubleValue() * cost.doubleValue() : 0.0;
                }).sum();

        double expiringSoonValue = items.stream()
                .filter(r -> "EXPIRING_SOON".equals(r.get("status")))
                .mapToDouble(r -> {
                    Number stock = (Number) r.get("stock");
                    Number cost = (Number) r.get("cost");
                    return stock != null && cost != null ? stock.doubleValue() * cost.doubleValue() : 0.0;
                }).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMonitored", items.size());
        summary.put("expiredCount", expiredCount);
        summary.put("expiringSoonCount", expiringSoonCount);
        summary.put("validCount", validCount);
        summary.put("expiredValue", Math.round(expiredValue * 100.0) / 100.0);
        summary.put("expiringSoonValue", Math.round(expiringSoonValue * 100.0) / 100.0);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("items", filtered);
        return response;
    }
}

package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ingredients")
public class Ingredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Stored in base unit only (e.g., "g", "ml", "pcs")
     */
    @Column(nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type")
    private UnitType unitType;

    /**
     * Stock quantity strictly stored in base unit
     */
    @Column(precision = 15, scale = 4)
    private BigDecimal stock = BigDecimal.ZERO;

    /**
     * Minimum stock threshold in base unit for low-stock alerts
     */
    @Column(name = "min_stock", precision = 15, scale = 4)
    private BigDecimal minStock = new BigDecimal("5.00");

    /**
     * Cost per base unit (e.g. $ per gram, $ per ml, $ per piece)
     */
    @Column(precision = 15, scale = 6)
    private BigDecimal cost = BigDecimal.ZERO;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<IngredientPurchaseUnit> purchaseUnits = new ArrayList<>();

    @Transient
    private Map<String, Long> count;

    public Ingredient(String name, String code, String unit, BigDecimal stock, BigDecimal minStock, BigDecimal cost) {
        this.name = name;
        this.code = code;
        this.unit = unit;
        this.stock = stock != null ? stock : BigDecimal.ZERO;
        this.minStock = minStock != null ? minStock : new BigDecimal("5.00");
        this.cost = cost != null ? cost : BigDecimal.ZERO;
    }

    public Ingredient(String name, String code, String unit, UnitType unitType, BigDecimal stock, BigDecimal minStock, BigDecimal cost) {
        this.name = name;
        this.code = code;
        this.unit = unit;
        this.unitType = unitType;
        this.stock = stock != null ? stock : BigDecimal.ZERO;
        this.minStock = minStock != null ? minStock : new BigDecimal("5.00");
        this.cost = cost != null ? cost : BigDecimal.ZERO;
    }
}

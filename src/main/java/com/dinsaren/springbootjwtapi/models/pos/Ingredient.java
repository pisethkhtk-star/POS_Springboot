package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(nullable = false)
    private String unit;

    @Column(precision = 10, scale = 2)
    private BigDecimal stock = BigDecimal.ZERO;

    @Column(name = "min_stock", precision = 10, scale = 2)
    private BigDecimal minStock = new BigDecimal("5.00");

    @Column(precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

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
}

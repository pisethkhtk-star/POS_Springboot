package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "units")
public class Unit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false)
    private UnitType unitType = UnitType.COUNT;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "name_km")
    private String nameKm;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "is_base", nullable = false)
    private Boolean isBase = false;

    @Column(name = "factor_to_base", nullable = false, precision = 15, scale = 6)
    private BigDecimal factorToBase = BigDecimal.ONE;

    private String description;

    public Unit(UnitType unitType, String name, String nameKm, String symbol, Boolean isBase, BigDecimal factorToBase) {
        this.unitType = unitType;
        this.name = name;
        this.nameKm = nameKm;
        this.symbol = symbol;
        this.isBase = isBase != null ? isBase : false;
        this.factorToBase = factorToBase != null ? factorToBase : BigDecimal.ONE;
    }

    public Unit(UnitType unitType, String name, String nameKm, String symbol, Boolean isBase, BigDecimal factorToBase, String description) {
        this.unitType = unitType;
        this.name = name;
        this.nameKm = nameKm;
        this.symbol = symbol;
        this.isBase = isBase != null ? isBase : false;
        this.factorToBase = factorToBase != null ? factorToBase : BigDecimal.ONE;
        this.description = description;
    }
}

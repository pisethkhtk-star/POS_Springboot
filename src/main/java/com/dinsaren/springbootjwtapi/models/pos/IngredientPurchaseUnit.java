package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ingredient_purchase_units")
public class IngredientPurchaseUnit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ingredient_id", nullable = false)
    private Integer ingredientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", insertable = false, updatable = false)
    @JsonIgnore
    private Ingredient ingredient;

    @Column(name = "unit_name", nullable = false)
    private String unitName; // e.g., "Bag", "Box", "Bottle", "Carton", "Can"

    @Column(name = "unit_name_km")
    private String unitNameKm; // e.g., "បាវ / កញ្ចប់", "ប្រអប់", "ដប", "កំប៉ុង"

    @Column(name = "factor_to_base", nullable = false, precision = 15, scale = 6)
    private BigDecimal factorToBase; // e.g. 1 Bag of beans = 1000g, 1 Can = 380g

    @Column(name = "default_cost", precision = 15, scale = 4)
    private BigDecimal defaultCost; // Default purchase cost per bag/box

    public IngredientPurchaseUnit(Integer ingredientId, String unitName, String unitNameKm, BigDecimal factorToBase, BigDecimal defaultCost) {
        this.ingredientId = ingredientId;
        this.unitName = unitName;
        this.unitNameKm = unitNameKm;
        this.factorToBase = factorToBase;
        this.defaultCost = defaultCost;
    }
}

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
@Table(name = "purchase_items")
public class PurchaseItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    @JsonIgnore
    private Purchase purchase;

    @Column(name = "ingredient_id", nullable = false)
    private Integer ingredientId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", insertable = false, updatable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public PurchaseItem(Purchase purchase, Ingredient ingredient, BigDecimal quantity, BigDecimal cost, BigDecimal subtotal) {
        this.purchase = purchase;
        this.ingredientId = ingredient.getId();
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.cost = cost;
        this.subtotal = subtotal;
    }
}

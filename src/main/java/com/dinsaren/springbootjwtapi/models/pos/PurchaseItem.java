package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(name = "ingredient_id", nullable = true)
    private Integer ingredientId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", insertable = false, updatable = false)
    private Ingredient ingredient;

    @Column(name = "product_id", nullable = true)
    private Integer productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private PosProduct product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "remaining_quantity", precision = 15, scale = 4)
    private BigDecimal remainingQuantity;

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity != null ? remainingQuantity : (quantity != null ? quantity : BigDecimal.ZERO);
    }

    public PurchaseItem(Purchase purchase, Ingredient ingredient, BigDecimal quantity, BigDecimal cost, BigDecimal subtotal) {
        this.purchase = purchase;
        this.ingredientId = ingredient.getId();
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.cost = cost;
        this.subtotal = subtotal;
    }

    public PurchaseItem(Purchase purchase, Ingredient ingredient, BigDecimal quantity, BigDecimal cost, BigDecimal subtotal, LocalDate expiryDate) {
        this.purchase = purchase;
        this.ingredientId = ingredient.getId();
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.cost = cost;
        this.subtotal = subtotal;
        this.expiryDate = expiryDate;
    }

    public PurchaseItem(Purchase purchase, PosProduct product, BigDecimal quantity, BigDecimal cost, BigDecimal subtotal) {
        this.purchase = purchase;
        this.productId = product.getId();
        this.product = product;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.cost = cost;
        this.subtotal = subtotal;
    }

    public PurchaseItem(Purchase purchase, PosProduct product, BigDecimal quantity, BigDecimal cost, BigDecimal subtotal, LocalDate expiryDate) {
        this.purchase = purchase;
        this.productId = product.getId();
        this.product = product;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
        this.cost = cost;
        this.subtotal = subtotal;
        this.expiryDate = expiryDate;
    }
}

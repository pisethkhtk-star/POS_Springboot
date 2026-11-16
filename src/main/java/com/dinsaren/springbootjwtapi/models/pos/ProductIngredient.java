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
@Table(name = "product_ingredients", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "ingredient_id"})
})
public class ProductIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private PosProduct product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    public ProductIngredient(PosProduct product, Ingredient ingredient, BigDecimal quantity) {
        this.product = product;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }
}

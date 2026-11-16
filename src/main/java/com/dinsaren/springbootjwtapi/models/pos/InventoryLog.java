package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import com.dinsaren.springbootjwtapi.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory_logs")
public class InventoryLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private PosProduct product;

    @Column(name = "ingredient_id")
    private Integer ingredientId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", insertable = false, updatable = false)
    private Ingredient ingredient;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "before_qty", precision = 10, scale = 2)
    private BigDecimal beforeQty;

    @Column(name = "after_qty", precision = 10, scale = 2)
    private BigDecimal afterQty;

    private String note;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}

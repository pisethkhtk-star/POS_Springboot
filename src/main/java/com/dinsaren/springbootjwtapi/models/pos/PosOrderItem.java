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
@Table(name = "order_items")
public class PosOrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private PosOrder order;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private PosProduct product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public PosOrderItem(PosOrder order, PosProduct product, Integer quantity, BigDecimal price, BigDecimal subtotal) {
        this.order = order;
        this.productId = product.getId();
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }
}

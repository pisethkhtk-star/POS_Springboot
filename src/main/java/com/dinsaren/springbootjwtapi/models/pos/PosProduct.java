package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class PosProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 14)
    private String code;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 5;

    @Column(name = "has_recipe", nullable = false)
    private Boolean hasRecipe = true;

    private String image;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("product")
    private List<ProductIngredient> ingredients = new ArrayList<>();

    public PosProduct(String name, String code, BigDecimal price, BigDecimal cost, Integer stock, Integer minStock, Boolean hasRecipe, String image, Integer categoryId) {
        this.name = name;
        this.code = code;
        this.price = price;
        this.cost = cost != null ? cost : BigDecimal.ZERO;
        this.stock = stock != null ? stock : 0;
        this.minStock = minStock != null ? minStock : 5;
        this.hasRecipe = hasRecipe != null ? hasRecipe : true;
        this.image = image;
        this.categoryId = categoryId;
    }
}

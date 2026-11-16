package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String symbol;

    private String description;

    public Unit(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public Unit(String name, String symbol, String description) {
        this.name = name;
        this.symbol = symbol;
        this.description = description;
    }
}

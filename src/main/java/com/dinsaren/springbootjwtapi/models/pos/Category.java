package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private String icon;

    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}

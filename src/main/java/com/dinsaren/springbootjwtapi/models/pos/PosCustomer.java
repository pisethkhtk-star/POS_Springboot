package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class PosCustomer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String phone;

    private String email;

    private String address;

    @Column(nullable = false)
    private Integer points = 0;

    public PosCustomer(String name, String phone, String email, String address, Integer points) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.points = points != null ? points : 0;
    }
}

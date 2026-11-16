package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String contact;

    private String phone;

    private String email;

    private String address;

    public Supplier(String name, String contact, String phone, String email, String address) {
        this.name = name;
        this.contact = contact;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }
}

package com.dinsaren.springbootjwtapi.models.pos;

import com.dinsaren.springbootjwtapi.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role_permissions")
public class RolePermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permission_pages", joinColumns = @JoinColumn(name = "role_permission_id"))
    @Column(name = "page")
    private List<String> pages = new ArrayList<>();

    public RolePermission(String role, List<String> pages) {
        this.role = role;
        this.pages = pages != null ? pages : new ArrayList<>();
    }
}

package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String email;
    private String name;
    private String role;
    private List<String> permissions;
    private Date createdAt;

    public UserResponseDTO(Integer id, String email, String name, String role, List<String> permissions) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.permissions = permissions;
    }
}

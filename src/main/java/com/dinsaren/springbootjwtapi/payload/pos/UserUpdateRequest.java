package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String email;
    private String password;
    private String role;
}

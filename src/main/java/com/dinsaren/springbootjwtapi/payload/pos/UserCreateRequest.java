package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String name;
    private String email;
    private String password;
    private String role;
}

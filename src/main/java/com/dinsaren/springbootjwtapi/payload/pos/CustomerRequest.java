package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;

@Data
public class CustomerRequest {
    private String name;
    private String phone;
    private String email;
    private String address;
    private Integer points;
}

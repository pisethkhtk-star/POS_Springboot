package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;

@Data
public class SupplierRequest {
    private String name;
    private String contact;
    private String phone;
    private String email;
    private String address;
}

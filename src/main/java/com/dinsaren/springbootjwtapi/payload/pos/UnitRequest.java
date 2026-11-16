package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;

@Data
public class UnitRequest {
    private String name;
    private String symbol;
    private String description;
}

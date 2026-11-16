package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.util.List;

@Data
public class PurchaseCreateRequest {
    private Integer supplierId;
    private String note;
    private String status; // defaults to "PENDING"
    private List<PurchaseItemRequest> items;
}

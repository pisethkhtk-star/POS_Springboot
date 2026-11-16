package com.dinsaren.springbootjwtapi.payload.pos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseReceiveRequest {
    private String note;
    private List<PurchaseReceiveItemRequest> items;

    @Data
    public static class PurchaseReceiveItemRequest {
        private Integer itemId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expiryDate;
    }
}

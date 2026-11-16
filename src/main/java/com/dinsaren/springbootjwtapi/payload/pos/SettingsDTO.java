package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDTO {
    private String storeName = "My POS System";
    private String storeAddress = "123 Main Street, Phnom Penh";
    private String storePhone = "012-345-678";
    private Double taxRate = 10.0;
    private String receiptHeader = "Thank you for shopping with us!";
    private String receiptFooter = "Please come again!";
}

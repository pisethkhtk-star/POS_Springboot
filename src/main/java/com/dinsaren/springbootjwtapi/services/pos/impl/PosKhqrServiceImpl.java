package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.services.pos.PosKhqrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class PosKhqrServiceImpl implements PosKhqrService {

    @Value("${bakong.account.id:sok.sao@acleda}")
    private String bakongAccountId;

    @Value("${bakong.merchant.name:Sok Sao Shop}")
    private String merchantName;

    @Value("${bakong.merchant.city:Phnom Penh}")
    private String merchantCity;

    @Override
    public Map<String, Object> generateKhqr(Map<String, Object> body) {
        Object amount = body != null ? body.getOrDefault("amount", 0) : 0;
        Object orderId = body != null ? body.getOrDefault("orderId", "POS-" + System.currentTimeMillis()) : "POS-" + System.currentTimeMillis();

        // EMVCo format string for KHQR
        String qrString = "00020101021229370016" + bakongAccountId + "520459995303840540" + amount +
                "5802KH5912" + (merchantName.length() > 25 ? merchantName.substring(0, 25) : merchantName) +
                "6010" + (merchantCity.length() > 15 ? merchantCity.substring(0, 15) : merchantCity) +
                "62200716" + orderId + "6304ABCD";

        return Map.of(
            "qrString", qrString,
            "amount", amount,
            "currency", "USD"
        );
    }
}

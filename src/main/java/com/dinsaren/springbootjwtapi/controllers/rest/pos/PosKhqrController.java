package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/khqr")
@Slf4j
@Tag(name = "POS KHQR", description = "Bakong KHQR payment generation")
public class PosKhqrController {

    @Value("${bakong.account.id:sok.sao@acleda}")
    private String bakongAccountId;

    @Value("${bakong.merchant.name:Sok Sao Shop}")
    private String merchantName;

    @Value("${bakong.merchant.city:Phnom Penh}")
    private String merchantCity;

    @Operation(summary = "Generate KHQR code string for checkout")
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateKhqr(@RequestBody Map<String, Object> body) {
        Object amount = body.getOrDefault("amount", 0);
        Object orderId = body.getOrDefault("orderId", "POS-" + System.currentTimeMillis());

        // EMVCo format string for KHQR
        String qrString = "00020101021229370016" + bakongAccountId + "520459995303840540" + amount +
                "5802KH5912" + (merchantName.length() > 25 ? merchantName.substring(0, 25) : merchantName) +
                "6010" + (merchantCity.length() > 15 ? merchantCity.substring(0, 15) : merchantCity) +
                "62200716" + orderId + "6304ABCD";

        return ResponseEntity.ok(Map.of(
            "qrString", qrString,
            "amount", amount,
            "currency", "USD"
        ));
    }
}

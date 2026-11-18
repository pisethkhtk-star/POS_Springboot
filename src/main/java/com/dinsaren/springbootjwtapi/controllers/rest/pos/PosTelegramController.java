package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.services.pos.PosTelegramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/telegram")
@Slf4j
@Tag(name = "POS Telegram", description = "Telegram bot receipt notifications")
@RequiredArgsConstructor
public class PosTelegramController {

    private final PosTelegramService telegramService;

    @Operation(summary = "Send receipt details to Telegram bot")
    @PostMapping("/send-receipt")
    public ResponseEntity<Map<String, Boolean>> sendReceipt(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(telegramService.sendReceipt(body));
    }
}

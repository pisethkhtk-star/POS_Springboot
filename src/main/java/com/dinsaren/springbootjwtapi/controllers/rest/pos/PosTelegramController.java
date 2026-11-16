package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/telegram")
@Slf4j
@Tag(name = "POS Telegram", description = "Telegram bot receipt notifications")
public class PosTelegramController {

    @Value("${telegram.bot.token:your_telegram_bot_token_here}")
    private String botToken;

    @Value("${telegram.chat.id:your_telegram_chat_id_here}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "Send receipt details to Telegram bot")
    @PostMapping("/send-receipt")
    public ResponseEntity<Map<String, Boolean>> sendReceipt(@RequestBody Map<String, Object> body) {
        if (botToken == null || chatId == null ||
            botToken.isEmpty() || chatId.isEmpty() ||
            "your_telegram_bot_token_here".equalsIgnoreCase(botToken) ||
            "your_telegram_chat_id_here".equalsIgnoreCase(chatId)) {
            log.warn("Telegram Bot Token or Chat ID not configured. Skipping notification.");
            return ResponseEntity.ok(Map.of("success", false));
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> order = (Map<String, Object>) body.get("order");
            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) body.get("settings");

            String storeName = settings != null && settings.get("storeName") != null ?
                    settings.get("storeName").toString() : "My POS Store";

            StringBuilder msg = new StringBuilder();
            msg.append("🧾 *NEW RECEIPT PRINTED*\n");
            msg.append("🏪 ").append(storeName).append("\n");
            msg.append("━━━━━━━━━━━━━━━━━━━━\n");
            msg.append("*Order #*: `").append(order.get("orderNumber")).append("`\n");
            msg.append("*Date*: ").append(order.get("createdAt") != null ? order.get("createdAt") : order.get("createAt")).append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) order.get("user");
            msg.append("*Cashier*: ").append(user != null && user.get("name") != null ? user.get("name") :
                    (user != null && user.get("username") != null ? user.get("username") : "Cashier")).append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> customer = (Map<String, Object>) order.get("customer");
            if (customer != null && customer.get("name") != null) {
                msg.append("*Customer*: ").append(customer.get("name"))
                   .append(" (").append(customer.get("phone") != null ? customer.get("phone") : "N/A").append(")\n");
            }

            msg.append("━━━━━━━━━━━━━━━━━━━━\n");
            msg.append("*ITEMS:*\n");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> product = (Map<String, Object>) item.get("product");
                    String pName = product != null && product.get("name") != null ? product.get("name").toString() : "Product";
                    msg.append("• ").append(pName)
                       .append(" x").append(item.get("quantity"))
                       .append(" = $").append(item.get("subtotal")).append("\n");
                }
            }

            msg.append("━━━━━━━━━━━━━━━━━━━━\n");
            msg.append("*Subtotal*: $").append(order.get("subtotal")).append("\n");
            if (order.get("discount") != null && Double.parseDouble(order.get("discount").toString()) > 0) {
                msg.append("*Discount*: -$").append(order.get("discount")).append("\n");
            }
            msg.append("*Tax*: $").append(order.get("tax")).append("\n");
            msg.append("*TOTAL*: $").append(order.get("total")).append("\n");
            msg.append("*Payment*: ").append(order.get("paymentMethod")).append("\n");

            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, Object> requestPayload = Map.of(
                "chat_id", chatId,
                "text", msg.toString(),
                "parse_mode", "Markdown"
            );

            restTemplate.postForObject(url, requestPayload, String.class);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Failed to send message to Telegram: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false));
        }
    }
}

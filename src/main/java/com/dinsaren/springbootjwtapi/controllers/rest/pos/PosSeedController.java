package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.startup.DataSeeder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
@Tag(name = "Data Seeder", description = "Endpoints to seed or verify POS sample data")
public class PosSeedController {

    private final DataSeeder dataSeeder;

    @Operation(summary = "Trigger POS data seeding (POST)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> seedPost() {
        return ResponseEntity.ok(dataSeeder.seedAll());
    }

    @Operation(summary = "Trigger POS data seeding (GET method for direct browser invocation)")
    @GetMapping
    public ResponseEntity<Map<String, Object>> seedGet() {
        return ResponseEntity.ok(dataSeeder.seedAll());
    }
}

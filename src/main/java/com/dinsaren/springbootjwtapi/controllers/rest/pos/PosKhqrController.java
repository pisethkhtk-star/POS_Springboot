package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.services.pos.PosKhqrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/khqr")
@Slf4j
@Tag(name = "POS KHQR", description = "Bakong KHQR payment generation")
@RequiredArgsConstructor
public class PosKhqrController {

    private final PosKhqrService khqrService;

    @Operation(summary = "Generate KHQR code string for checkout")
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateKhqr(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(khqrService.generateKhqr(body));
    }
}

package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.payload.pos.SettingsDTO;
import com.dinsaren.springbootjwtapi.services.pos.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/settings")
@Slf4j
@Tag(name = "POS Settings", description = "Store receipt and business configuration")
@RequiredArgsConstructor
public class PosSettingController {

    private final SettingService settingService;

    @Operation(summary = "Get store settings")
    @GetMapping
    public ResponseEntity<SettingsDTO> getSettings() {
        return ResponseEntity.ok(settingService.getSettings());
    }

    @Operation(summary = "Update store settings")
    @PostMapping
    public ResponseEntity<SettingsDTO> updateSettings(@RequestBody SettingsDTO request) {
        return ResponseEntity.ok(settingService.updateSettings(request));
    }
}

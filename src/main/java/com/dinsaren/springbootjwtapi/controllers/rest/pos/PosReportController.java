package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.services.pos.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
@Slf4j
@Tag(name = "POS Reports", description = "Sales and profit report analytics")
@RequiredArgsConstructor
public class PosReportController {

    private final ReportService reportService;

    @Operation(summary = "Generate sales, profit and revenue reports")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getReports(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(reportService.getReports(startDate, endDate));
    }

    @Operation(summary = "Generate product and ingredient expiry tracking report sorted by expiry date")
    @GetMapping("/expiry")
    public ResponseEntity<Map<String, Object>> getExpiryReport(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(reportService.getExpiryReport(search, type, status));
    }
}

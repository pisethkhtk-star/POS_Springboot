package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import com.dinsaren.springbootjwtapi.payload.pos.OrderCreateRequest;
import com.dinsaren.springbootjwtapi.security.services.UserDetailsImpl;
import com.dinsaren.springbootjwtapi.services.pos.PosOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
@Slf4j
@Tag(name = "POS Orders", description = "Manage customer sales orders and checkout")
@RequiredArgsConstructor
public class PosOrderController {

    private final PosOrderService orderService;

    @Operation(summary = "Search and filter orders")
    @GetMapping
    public ResponseEntity<List<PosOrder>> search(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate
    ) {
        return ResponseEntity.ok(orderService.searchOrders(search, status, startDate, endDate));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PosOrder> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Create customer checkout order")
    @PostMapping
    public ResponseEntity<PosOrder> create(
        @RequestBody OrderCreateRequest request,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request, userId));
    }

    @Operation(summary = "Cancel or refund order and return stock")
    @PutMapping("/{id}")
    public ResponseEntity<PosOrder> updateStatus(
        @PathVariable Integer id,
        @RequestBody Map<String, String> body,
        @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    ) {
        Integer userId = user != null ? user.getId() : 1;
        String status = body.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, userId));
    }
}

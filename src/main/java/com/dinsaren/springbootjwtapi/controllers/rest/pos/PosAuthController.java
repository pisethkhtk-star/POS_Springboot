package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.payload.pos.LoginRequest;
import com.dinsaren.springbootjwtapi.services.pos.PosAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "POS Auth", description = "Authentication for POS terminal")
public class PosAuthController {

    private final PosAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with email and password")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info and permissions")
    public ResponseEntity<Map<String, Object>> me(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) Authentication authentication,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) HttpServletRequest request,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication, request, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out user and clear auth cookie")
    public ResponseEntity<Map<String, Boolean>> logout(@io.swagger.v3.oas.annotations.Parameter(hidden = true) HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(response));
    }
}

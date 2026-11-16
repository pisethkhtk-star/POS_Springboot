package com.dinsaren.springbootjwtapi.controllers.rest.pos;

import com.dinsaren.springbootjwtapi.models.User;
import com.dinsaren.springbootjwtapi.payload.pos.UserCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.UserUpdateRequest;
import com.dinsaren.springbootjwtapi.services.pos.PosUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "POS Users", description = "User management for POS system")
public class PosUserController {

    private final PosUserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<User> create(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<User> update(@PathVariable Integer id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

package com.dinsaren.springbootjwtapi.services.pos.impl;

import com.dinsaren.springbootjwtapi.constants.Constants;
import com.dinsaren.springbootjwtapi.models.Role;
import com.dinsaren.springbootjwtapi.models.User;
import com.dinsaren.springbootjwtapi.models.UserRole;
import com.dinsaren.springbootjwtapi.payload.pos.UserCreateRequest;
import com.dinsaren.springbootjwtapi.payload.pos.UserUpdateRequest;
import com.dinsaren.springbootjwtapi.repository.RoleRepository;
import com.dinsaren.springbootjwtapi.repository.UserRepository;
import com.dinsaren.springbootjwtapi.repository.pos.PosOrderRepository;
import com.dinsaren.springbootjwtapi.services.pos.PosUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PosUserServiceImpl implements PosUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PosOrderRepository posOrderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private Role getOrCreateRole(String roleStr) {
        String clean = roleStr != null ? roleStr.toUpperCase().replace("ROLE_", "") : "CASHIER";
        UserRole userRole;
        try {
            userRole = UserRole.valueOf("ROLE_" + clean);
        } catch (IllegalArgumentException e) {
            userRole = UserRole.ROLE_CASHIER;
        }

        UserRole finalRole = userRole;
        return roleRepository.findByName(finalRole)
                .orElseGet(() -> roleRepository.save(new Role(finalRole)));
    }

    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        if (request.getName() == null || request.getEmail() == null ||
            request.getPassword() == null || request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setName(request.getName().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(Constants.STATUS_ACTIVE);
        user.setChangePassword(Constants.No);

        Role role = getOrCreateRole(request.getRole());
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        if (request.getPermissions() != null) {
            user.setPermissions(request.getPermissions());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Integer id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getEmail() != null) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email address is already in use");
            }
            user.setEmail(newEmail);
            if (user.getUsername() == null || user.getUsername().equals(user.getEmail())) {
                user.setUsername(newEmail);
            }
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getRole() != null) {
            Role role = getOrCreateRole(request.getRole());
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        if (request.getPermissions() != null) {
            user.setPermissions(request.getPermissions());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long orderCount = posOrderRepository.countByUserId(id);
        if (orderCount > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete user with existing sales records");
        }

        userRepository.delete(user);
    }
}

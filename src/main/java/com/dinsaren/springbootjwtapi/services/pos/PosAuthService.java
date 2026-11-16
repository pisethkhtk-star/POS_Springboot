package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.models.User;
import com.dinsaren.springbootjwtapi.payload.pos.LoginRequest;
import com.dinsaren.springbootjwtapi.payload.pos.UserResponseDTO;
import com.dinsaren.springbootjwtapi.repository.UserRepository;
import com.dinsaren.springbootjwtapi.security.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PosAuthService {

    private final UserRepository userRepository;
    private final RolePermissionService rolePermissionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public Map<String, Object> login(LoginRequest request, HttpServletResponse response) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        String searchKey = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(searchKey)
                .or(() -> userRepository.findByUsername(searchKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        List<String> permissions = rolePermissionService.getDefaultPermissions(user.getRole());
        String token = jwtUtils.generateTokenFromUsername(user.getUsername() != null ? user.getUsername() : user.getEmail());

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        UserResponseDTO userDTO = new UserResponseDTO(user.getId(), user.getEmail(), user.getName(), user.getRole(), permissions, user.getCreatedAt());
        return Map.of("user", userDTO, "token", token);
    }

    public Map<String, Object> getCurrentUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        String username = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
        } else if (request != null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    if (jwtUtils.validateJwtToken(cookie.getValue())) {
                        username = jwtUtils.getUserNameFromJwtToken(cookie.getValue());
                        break;
                    }
                }
            }
        }

        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String finalUsername = username;
        User user = userRepository.findByUsername(finalUsername)
                .or(() -> userRepository.findByEmail(finalUsername))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<String> permissions = rolePermissionService.getDefaultPermissions(user.getRole());
        String token = jwtUtils.generateTokenFromUsername(user.getUsername() != null ? user.getUsername() : user.getEmail());

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(86400)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        UserResponseDTO userDTO = new UserResponseDTO(user.getId(), user.getEmail(), user.getName(), user.getRole(), permissions, user.getCreatedAt());
        return Map.of("user", userDTO, "token", token);
    }

    public Map<String, Boolean> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return Map.of("success", true);
    }
}

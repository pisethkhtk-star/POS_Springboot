package com.dinsaren.springbootjwtapi.services.pos;

import com.dinsaren.springbootjwtapi.payload.pos.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface PosAuthService {
    Map<String, Object> login(LoginRequest request, HttpServletResponse response);
    Map<String, Object> getCurrentUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response);
    Map<String, Boolean> logout(HttpServletResponse response);
}

package com.chargev.emsp.config;

import org.springframework.web.servlet.HandlerInterceptor;

import com.chargev.emsp.service.cryptography.JwtTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthInterceptorForPnc implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;

    public AuthInterceptorForPnc(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"resultCode\": \"401\", \"resultMsg\": \"Unauthorized: Missing or invalid Authorization header\"}");
            return false;
        }

        String token = authorization.substring(7);
        if (!jwtTokenService.validateToken(token, "PNC", "*")) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"resultCode\": \"401\", \"resultMsg\": \"Unauthorized: Invalid token\"}");
            return false;
        }

        return true;
    }
}

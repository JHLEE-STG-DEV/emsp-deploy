package com.chargev.emsp.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.chargev.emsp.model.dto.response.PncApiResponse;
import com.chargev.emsp.model.dto.response.PncResponseResult;
import com.chargev.emsp.service.cryptography.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptorForPnc implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptorForPnc(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = new ObjectMapper(); // Jackson ObjectMapper
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            PncApiResponse apiResponse = new PncApiResponse();
            apiResponse.setResult(PncResponseResult.FAIL);
            apiResponse.setCode("401");
            apiResponse.setMessage("Unauthorized: Missing or invalid Authorization header.");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }

        String token = authorization.substring(7);
        if (!jwtTokenService.validateToken(token, "PNC", "*")) {
            PncApiResponse apiResponse = new PncApiResponse();
            apiResponse.setResult(PncResponseResult.FAIL);
            apiResponse.setCode("401");
            apiResponse.setMessage("Unauthorized: Invalid token.");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }

        return true;
    }
}

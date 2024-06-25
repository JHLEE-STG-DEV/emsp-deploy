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
public class VersionInterceptorForPnc implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public VersionInterceptorForPnc(JwtTokenService jwtTokenService) {
        this.objectMapper = new ObjectMapper(); // Jackson ObjectMapper
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String version = request.getRequestURI().split("/")[1];
        
        if (!("V001".equals(version) || "v001".equals(version))) {
            PncApiResponse apiResponse = new PncApiResponse();
            apiResponse.setResult(PncResponseResult.FAIL);
            apiResponse.setCode("400");
            apiResponse.setMessage("Unsupported version.");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;        }

        return true;
    }
}

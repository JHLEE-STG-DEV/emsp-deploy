package com.chargev.emsp.interceptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.cryptography.JwtTokenService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthInterceptorForOem implements HandlerInterceptor {
    private final JwtTokenService jwtTokenService;
    private final DateTimeFormatterService dateTimeFormatterService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            ApiResponseString apiResponse = new ApiResponseString();
            apiResponse.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            apiResponse.setStatusMessage("Unauthorized: Missing or invalid Authorization header");
            apiResponse.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            apiResponse.setData(null);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }

        String token = authorization.substring(7);
        if (!jwtTokenService.validateToken(token, "OEM", "WRITE:OEMAPI_GENERAL;READ:OEMAPI_GENERAL")) {
            ApiResponseString apiResponse = new ApiResponseString();
            apiResponse.setStatusCode(OcpiResponseStatusCode.UNKNOWN_TOKEN);
            apiResponse.setStatusMessage("Unauthorized: Invalid token");
            apiResponse.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            apiResponse.setData(null);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }

        return true;
    }
}

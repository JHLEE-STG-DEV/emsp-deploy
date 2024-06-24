package com.chargev.emsp.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class VersionInterceptorForPnc implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String version = request.getRequestURI().split("/")[1];
        
        if (!"V001".equals(version)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status_code\": \"400\", \"status_message\": \"Unsupported version\", \"timestamp\": \"null\"}");
            return false;
        }

        return true;
    }
}

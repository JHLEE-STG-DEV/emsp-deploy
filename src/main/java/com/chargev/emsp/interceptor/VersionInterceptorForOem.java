package com.chargev.emsp.interceptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class VersionInterceptorForOem implements HandlerInterceptor {
    private final DateTimeFormatterService dateTimeFormatterService;
    private final ObjectMapper objectMapper;

    // @Autowired
    // public VersionInterceptorForOem(DateTimeFormatterService dateTimeFormatterService) {
    //     this.dateTimeFormatterService = dateTimeFormatterService;
    //     this.objectMapper = new ObjectMapper(); // Jackson ObjectMapper
    // }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String version = request.getRequestURI().split("/")[1];
        
        if (!("V001".equals(version) || "v001".equals(version))) {
            ApiResponseString apiResponse = new ApiResponseString();
            apiResponse.setStatusCode(OcpiResponseStatusCode.UNSUPPORTED_VERSION);
            apiResponse.setStatusMessage("Unsupported version");
            apiResponse.setTimestamp(dateTimeFormatterService.formatToCustomStyle(ZonedDateTime.now(ZoneId.of("UTC"))));
            apiResponse.setData(null);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return false;
        }

        return true;
    }
}

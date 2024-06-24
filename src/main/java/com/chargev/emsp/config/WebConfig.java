package com.chargev.emsp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chargev.emsp.service.cryptography.JwtTokenService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtTokenService jwtTokenService;
    private final DateTimeFormatterService dateTimeFormatterService;

    @Autowired
    public WebConfig(JwtTokenService jwtTokenService, DateTimeFormatterService dateTimeFormatterService) {
        this.jwtTokenService = jwtTokenService;
        this.dateTimeFormatterService = dateTimeFormatterService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptorForPnc(jwtTokenService))
                .addPathPatterns("/{version}/pnc/**");  // PncController에 적용
        registry.addInterceptor(new AuthInterceptorForOem(jwtTokenService, dateTimeFormatterService))
                .addPathPatterns("/{version}/oem/**");  // OemController에 적용 
    }


}

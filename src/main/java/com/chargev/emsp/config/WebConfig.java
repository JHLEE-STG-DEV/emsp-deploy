package com.chargev.emsp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chargev.emsp.interceptor.AuthInterceptorForOem;
import com.chargev.emsp.interceptor.AuthInterceptorForPnc;
import com.chargev.emsp.interceptor.VersionInterceptorForOem;
import com.chargev.emsp.interceptor.VersionInterceptorForPnc;
import com.chargev.emsp.service.cryptography.JwtTokenService;
import com.chargev.emsp.service.formatter.DateTimeFormatterService;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtTokenService jwtTokenService;
    private final DateTimeFormatterService dateTimeFormatterService;
    private final VersionInterceptorForOem versionInterceptorForOem;
    private final VersionInterceptorForPnc versionInterceptorForPnc;
    private final AuthInterceptorForPnc authInterceptorForPnc;
    private final AuthInterceptorForOem authInterceptorForOem;

    @Autowired
    public WebConfig(JwtTokenService jwtTokenService,
                     DateTimeFormatterService dateTimeFormatterService,
                     VersionInterceptorForOem versionInterceptorForOem,
                     VersionInterceptorForPnc versionInterceptorForPnc,
                     AuthInterceptorForPnc authInterceptorForPnc,
                     AuthInterceptorForOem authInterceptorForOem) {
        this.jwtTokenService = jwtTokenService;
        this.dateTimeFormatterService = dateTimeFormatterService;
        this.versionInterceptorForOem = versionInterceptorForOem;
        this.versionInterceptorForPnc = versionInterceptorForPnc;
        this.authInterceptorForPnc = authInterceptorForPnc;
        this.authInterceptorForOem = authInterceptorForOem;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptorForPnc)
                .addPathPatterns("/{version}/pnc/**");  // PncController에 적용
        registry.addInterceptor(authInterceptorForOem)
                .addPathPatterns("/{version}/oem/**");  // OemController에 적용 
        registry.addInterceptor(versionInterceptorForOem)
                .addPathPatterns("/{version}/oem/**");
        registry.addInterceptor(versionInterceptorForPnc)
                .addPathPatterns("/{version}/pnc/**");
    }
}

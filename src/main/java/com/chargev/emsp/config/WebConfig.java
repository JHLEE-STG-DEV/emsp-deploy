package com.chargev.emsp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chargev.emsp.interceptor.AuthInterceptorForCpo;
import com.chargev.emsp.interceptor.AuthInterceptorForOcpi;
import com.chargev.emsp.interceptor.AuthInterceptorForOem;
import com.chargev.emsp.interceptor.AuthInterceptorForPnc;
import com.chargev.emsp.interceptor.VersionInterceptorForOem;
import com.chargev.emsp.interceptor.VersionInterceptorForPnc;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final VersionInterceptorForOem versionInterceptorForOem;
    private final VersionInterceptorForPnc versionInterceptorForPnc;
    private final AuthInterceptorForPnc authInterceptorForPnc;
    private final AuthInterceptorForOem authInterceptorForOem;
    private final AuthInterceptorForOcpi authInterceptorForOcpi;
    private final AuthInterceptorForCpo authInterceptorForCpo;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptorForPnc)
                .addPathPatterns("/{version}/pnc/**");  // PncController에 적용
        registry.addInterceptor(authInterceptorForOem)
                .addPathPatterns("/{version}/oem/**");  // OemController에 적용 
        registry.addInterceptor(authInterceptorForOcpi)
                .addPathPatterns("/{version}/ocpi/**");  // OcpiController에 적용 
        registry.addInterceptor(authInterceptorForCpo)
                .addPathPatterns("/{version}/cpo/**");  // CpoController에 적용 
        registry.addInterceptor(versionInterceptorForPnc)
                .addPathPatterns("/{version}/pnc/**");
        registry.addInterceptor(versionInterceptorForPnc)
                .addPathPatterns("/{version}/cpo/**"); // version interceptor는 pnc, cpo 공통으로 사용 (응답 형식에 따라서만 구분)
        registry.addInterceptor(versionInterceptorForOem)
                .addPathPatterns("/{version}/oem/**");
        registry.addInterceptor(versionInterceptorForOem)
                .addPathPatterns("/{version}/ocpi/**"); // version interceptor는 ocpi, oem 공통으로 사용 (응답 형식에 따라서만 구분)
    }
}

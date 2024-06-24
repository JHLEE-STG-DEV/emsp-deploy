package com.chargev.emsp;

import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Chargev OCPI API",
                description = "Chargev OCPI API Prototypes"
        )
)

public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = getSecurityScheme();
        SecurityRequirement securityRequirement = getSecurityRequireMent();

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(List.of(securityRequirement));
    }

    private SecurityScheme getSecurityScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
    }

    private SecurityRequirement getSecurityRequireMent() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    @Bean
    public OperationCustomizer addGlobalHeaders() {
        return (operation, handlerMethod) -> {
            Parameter versionParameter = new Parameter()
                    .in("path")
                    .name("version")
                    .required(true)
                    .description("API version")
                    .schema(new io.swagger.v3.oas.models.media.StringSchema());
            operation.addParametersItem(versionParameter);
            return operation;
        };
    }
}
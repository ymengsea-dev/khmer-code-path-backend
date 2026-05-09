package com.mengsea.khmercodepath.commons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenApi() {
        final String securitySchemaName = "bearer Auth";
        return new OpenAPI()
                .info(new Info()
                        .title("School Management")
                        .version("1.0.0")
                        .description("School Management Documentation"))
                .components(new Components()
                        .addSecuritySchemes(securitySchemaName,
                                new SecurityScheme()
                                        .name(securitySchemaName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("Bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication. Enter your JWT token in the format: Bearer {token}")));
    }
}

package com.helpdesk.ticket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticket Service API")
                        .version("1.0")
                        .description("""
                                IT Helpdesk Ticket Management Service with JWT Authentication
                                
                                **To use protected endpoints:**
                                1. Login at /auth/login
                                2. Copy the token from response
                                3. Click 'Authorize' button above
                                4. Enter: Bearer {your-token}
                                
                                **Demo Users:**
                                - Username: john.doe, Password: password123
                                - Username: admin, Password: admin123
                                - Username: it.support, Password: support123
                                """)
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@company.com")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /auth/login endpoint")));
    }
}
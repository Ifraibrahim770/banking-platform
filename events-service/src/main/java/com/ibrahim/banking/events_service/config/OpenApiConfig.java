package com.ibrahim.banking.events_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8084");
        devServer.setDescription("Server URL in Development environment");

        Contact contact = new Contact();
        contact.setName("Banking System Support");
        contact.setEmail("support@bankingsystem.com");

        License license = new License()
                .name("Apache License, Version 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Events Service API")
                .version("1.0.0")
                .contact(contact)
                .description("This API provides endpoints to manage system events and notifications.")
                .license(license);

        // Define the security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // Add the security scheme to components
        Components components = new Components()
                .addSecuritySchemes("Bearer Authentication", securityScheme);

        // Add the security requirement to the OpenAPI object
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
} 
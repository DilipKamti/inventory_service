package com.microservice.inventory.service.utility;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Inventory Service API", version = "1.0", description = "Inventory service for product stock")
)
public class SwaggerConfig {}

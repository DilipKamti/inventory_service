package com.microservice.inventory.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryRequest(
        @NotBlank(message = "Product code is required")
        String productCode,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}



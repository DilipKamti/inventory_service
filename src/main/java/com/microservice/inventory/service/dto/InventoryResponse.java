package com.microservice.inventory.service.dto;

public record InventoryResponse(String productCode, boolean inStock, int availableQuantity) {}

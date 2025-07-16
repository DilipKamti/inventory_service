package com.microservice.inventory.service.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.microservice.inventory.service.dto.InventoryRequest;
import com.microservice.inventory.service.dto.InventoryResponse;

public interface InventoryService {
    InventoryResponse checkStock(String productCode);
    void reduceStock(String productCode, int quantity);
    InventoryResponse addInventory(InventoryRequest request);
    Page<InventoryResponse> getAllInventories(Pageable pageable);

}



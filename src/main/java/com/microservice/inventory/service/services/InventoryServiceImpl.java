package com.microservice.inventory.service.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.microservice.inventory.service.dto.InventoryRequest;
import com.microservice.inventory.service.dto.InventoryResponse;
import com.microservice.inventory.service.model.Inventory;
import com.microservice.inventory.service.repository.InventoryRepository;
import com.microservice.inventory.service.utility.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository inventoryRepository;

	@Override
	@Cacheable(value = "stock", key = "#productCode")
	public InventoryResponse checkStock(String productCode) {
		Inventory inventory = inventoryRepository.findByProductCode(productCode)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productCode));

		return new InventoryResponse(inventory.getProductCode(), inventory.getQuantity() > 0, inventory.getQuantity());
	}

	@Override
	public void reduceStock(String productCode, int quantity) {
		Inventory inventory = inventoryRepository.findByProductCode(productCode)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productCode));

		if (inventory.getQuantity() < quantity) {
			throw new IllegalArgumentException("Not enough stock for product: " + productCode);
		}

		inventory.setQuantity(inventory.getQuantity() - quantity);
		inventoryRepository.save(inventory);
	}

	@Override
	public InventoryResponse addInventory(InventoryRequest request) {
		Inventory existing = inventoryRepository.findByProductCode(request.productCode()).orElse(null);

		if (existing != null) {
			existing.setQuantity(existing.getQuantity() + request.quantity());
			inventoryRepository.save(existing);
			return new InventoryResponse(existing.getProductCode(), true, existing.getQuantity());
		}

		Inventory inventory = Inventory.builder().productCode(request.productCode()).quantity(request.quantity())
				.build();
		Inventory saved = inventoryRepository.save(inventory);
		return new InventoryResponse(saved.getProductCode(), true, saved.getQuantity());
	}

	@Override
	public Page<InventoryResponse> getAllInventories(Pageable pageable) {
		return inventoryRepository.findAll(pageable)
				.map(inv -> new InventoryResponse(inv.getProductCode(), inv.getQuantity() > 0, inv.getQuantity()));
	}

}

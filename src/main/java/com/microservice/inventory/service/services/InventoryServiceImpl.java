package com.microservice.inventory.service.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	public InventoryResponse addInventory(InventoryRequest request) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addInventory'");
	}

	@Override
	@Cacheable(value = "stock", key = "#productCode")
	public InventoryResponse checkStock(String productCode) {
		log.info("Checking stock for productCode: {}", productCode);
		Inventory inventory = inventoryRepository.findByProductCode(productCode)
				.orElseThrow(() -> {
					log.warn("Product not found in inventory: {}", productCode);
					return new ResourceNotFoundException("Product not found: " + productCode);
				});

		boolean inStock = inventory.getQuantity() > 0;
		log.info("Product {} is {} with quantity: {}", productCode, inStock ? "in stock" : "out of stock",
				inventory.getQuantity());
		return new InventoryResponse(inventory.getProductCode(), inStock, inventory.getQuantity());
	}

	@Override
	public void reduceStock(String productCode, int quantity) {
		log.info("Reducing stock for productCode: {} by quantity: {}", productCode, quantity);
		Inventory inventory = inventoryRepository.findByProductCode(productCode)
				.orElseThrow(() -> {
					log.error("Attempt to reduce stock failed. Product not found: {}", productCode);
					return new ResourceNotFoundException("Product not found: " + productCode);
				});

		if (inventory.getQuantity() < quantity) {
			log.error("Not enough stock for product: {}. Available: {}, Requested: {}", productCode,
					inventory.getQuantity(), quantity);
			throw new IllegalArgumentException("Not enough stock for product: " + productCode);
		}

		inventory.setQuantity(inventory.getQuantity() - quantity);
		inventoryRepository.save(inventory);
		log.info("Stock updated for productCode: {}. Remaining quantity: {}", productCode, inventory.getQuantity());
	}

	@Override
	public List<InventoryResponse> addInventoryBatch(List<InventoryRequest> requests) {
		log.info("Processing batch inventory update. Total records: {}", requests.size());

		Map<String, Inventory> existingMap = inventoryRepository
				.findAllByProductCodeIn(
						requests.stream().map(InventoryRequest::productCode).toList())
				.stream()
				.collect(Collectors.toMap(Inventory::getProductCode, inv -> inv));

		List<Inventory> toSave = new ArrayList<>();
		List<InventoryResponse> responses = new ArrayList<>();

		for (InventoryRequest request : requests) {
			Inventory inventory = existingMap.get(request.productCode());

			if (inventory != null) {
				inventory.setQuantity(inventory.getQuantity() + request.quantity());
				log.info("Updating existing inventory: {}, New quantity: {}", request.productCode(),
						inventory.getQuantity());
			} else {
				inventory = Inventory.builder()
						.productCode(request.productCode())
						.quantity(request.quantity())
						.build();
				log.info("Creating new inventory: {}, Quantity: {}", request.productCode(), request.quantity());
			}

			toSave.add(inventory);
		}

		inventoryRepository.saveAll(toSave);

		for (Inventory inv : toSave) {
			responses.add(new InventoryResponse(inv.getProductCode(), inv.getQuantity() > 0, inv.getQuantity()));
		}

		log.info("Batch inventory update completed. Total saved: {}", toSave.size());
		return responses;
	}

	@Override
	public Page<InventoryResponse> getAllInventories(Pageable pageable) {
		log.info("Fetching all inventory entries, page: {}, size: {}", pageable.getPageNumber(),
				pageable.getPageSize());
		return inventoryRepository.findAll(pageable)
				.map(inv -> {
					boolean inStock = inv.getQuantity() > 0;
					log.debug("Mapped inventory: {} with quantity: {}, inStock: {}", inv.getProductCode(),
							inv.getQuantity(), inStock);
					return new InventoryResponse(inv.getProductCode(), inStock, inv.getQuantity());
				});
	}

@Override
public List<InventoryResponse> checkProductInStockByQuantity(List<InventoryRequest> requests) {
    List<String> productCodes = requests.stream()
        .map(InventoryRequest::productCode)
        .toList();

    List<Inventory> inventoryList = inventoryRepository.findAllByProductCodeIn(productCodes);

	return inventoryList.stream()
		.map(product -> {
			InventoryRequest matchedRequest = requests.stream()
				.filter(r -> r.productCode().equals(product.getProductCode()))
				.findFirst()
				.orElse(null);

			boolean inStock = matchedRequest != null && product.getQuantity() >= matchedRequest.quantity();

			return new InventoryResponse(product.getProductCode(), inStock, product.getQuantity());
		})
		.toList();
}


}

package com.microservice.inventory.service.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.inventory.service.dto.ApiResponse;
import com.microservice.inventory.service.dto.InventoryRequest;
import com.microservice.inventory.service.dto.InventoryResponse;
import com.microservice.inventory.service.services.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Inventory Controller", description = "APIs to manage inventory")
public class InventoryController {

	private final InventoryService inventoryService;

	@GetMapping("/{productCode}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Check stock availability")
	public ResponseEntity<ApiResponse<InventoryResponse>> checkStock(@PathVariable String productCode) {
		log.info("Checking stock for product: {}", productCode);
		InventoryResponse response = inventoryService.checkStock(productCode);
		return ResponseEntity.ok(ApiResponse.success(response, "Stock checked successfully"));
	}

	@PostMapping("/batch")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Add inventory in batch")
	public ResponseEntity<ApiResponse<List<InventoryResponse>>> addStockBatch(
			@Valid @RequestBody List<InventoryRequest> requests) {

		log.info("Received batch inventory request. Size: {}", requests.size());

		List<InventoryResponse> responses = inventoryService.addInventoryBatch(requests);

		return ResponseEntity.ok(ApiResponse.success(responses, "Batch inventory update successful"));
	}

	@PutMapping("/reduce")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@Operation(summary = "Reduce stock")
	public ResponseEntity<ApiResponse<Void>> reduceStock(@Valid @RequestBody InventoryRequest request) {
		log.info("Reducing inventory for: {}", request);
		inventoryService.reduceStock(request.productCode(), request.quantity());
		return ResponseEntity.ok(ApiResponse.success(null, "Stock reduced successfully"));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@Operation(summary = "Get all inventory items with pagination")
	public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getAllInventories(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

		Page<InventoryResponse> inventoryPage = inventoryService.getAllInventories(PageRequest.of(page, size));
		return ResponseEntity.ok(ApiResponse.success(inventoryPage, "Inventories fetched"));
	}

	@PostMapping("/batch/check")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	@Operation(summary = "Check stock availability for multiple products")
	public ResponseEntity<ApiResponse<List<InventoryResponse>>> checkProductInStock(@RequestBody List<InventoryRequest> productList) {
		log.info("Checking stock for product(s): {}", productList);
		List<InventoryResponse> response = inventoryService.checkProductInStockByQuantity(productList);
		return ResponseEntity.ok(ApiResponse.success(response, "Stock checked successfully"));
	}

}

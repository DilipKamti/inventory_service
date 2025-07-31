package com.microservice.inventory.service.kafka;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.microservice.inventory.service.dto.InventoryRequest;
import com.microservice.inventory.service.dto.InventoryResponse;
import com.microservice.inventory.service.services.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

	private final InventoryService inventoryService;
	private final KafkaProducerService kafkaProducerService;

	@KafkaListener(topics = KafkaTopics.PRODUCT_CREATION_IN_INVENTORY, groupId = "inventory-service-group")
	public void handleProductCreationEvent(List<InventoryRequest> inventoryRequests) {
		log.info("📥 Received product creation event with {} inventory requests", inventoryRequests.size());

		try {
			List<InventoryResponse> processedList = inventoryService.addInventoryBatch(inventoryRequests);

			if (!processedList.isEmpty() && processedList.size() == inventoryRequests.size()) {
				log.info("✅ All inventory entries processed successfully.");

				Map<String, Object> response = KafkaResponseBuilder.create().status("SUCCESS").data(processedList)
						.build();

				kafkaProducerService.send(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY_RESPONSE, response);

			} else if (!processedList.isEmpty()) {
				log.warn("⚠️ Partial inventory update. Expected: {}, Processed: {}", inventoryRequests.size(),
						processedList.size());

				Set<String> successCodes = processedList.stream().map(InventoryResponse::productCode)
						.collect(Collectors.toSet());

				List<String> partialSuccess = inventoryRequests.stream().map(InventoryRequest::productCode)
						.filter(successCodes::contains).toList();

				Map<String, Object> response = KafkaResponseBuilder.create().status("PARTIAL_SUCCESS")
						.data(partialSuccess).build();

				kafkaProducerService.send(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY_RESPONSE, response);

			} else {
				log.error("❌ Failed to process any inventory request.");

				Map<String, Object> response = KafkaResponseBuilder.create().status("FAIL").build();

				kafkaProducerService.send(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY_RESPONSE, response);
			}
		} catch (Exception ex) {
			log.error("💥 Exception while processing inventory requests: {}", ex.getMessage(), ex);

			Map<String, Object> response = KafkaResponseBuilder.create().status("FAIL").build();

			kafkaProducerService.send(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY_RESPONSE, response);

			throw ex;
		}
	}
}

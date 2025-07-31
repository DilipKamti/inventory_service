package com.microservice.inventory.service.kafka;

import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.microservice.inventory.service.dto.InventoryRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Object payload) {
        kafkaTemplate.send(topic, payload);
        log.info("📦 Message sent to topic [{}]: {}", topic, payload);
    }

    public void sendToInventory(List<InventoryRequest> inventoryRequests) {
    	kafkaTemplate.send(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY, inventoryRequests);
    }

}

package com.microservice.inventory.service.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class kafkaConfig {
    

    @KafkaListener(topics = KafkaTopicConstants.PRODUCT_CREATION_IN_INVENTORY,groupId = "inventory-service-group")
    public void configureKafka(Object message) {
        System.out.println("Received message: " + message);
    }
}

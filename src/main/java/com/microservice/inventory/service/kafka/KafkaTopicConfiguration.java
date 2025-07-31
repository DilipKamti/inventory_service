package com.microservice.inventory.service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

	@Value("${kafka.topic.partitions:1}")
    private int numPartitions;

    @Value("${kafka.topic.replicas:1}")
    private short numReplicas;
	
    @Bean
    public NewTopic productCreationTopic() {
        return TopicBuilder.name(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY)
                .partitions(numPartitions)
                .replicas(numReplicas)
                .build();
    }

    @Bean
    public NewTopic productCreationResponseTopic() {
        return TopicBuilder.name(KafkaTopics.PRODUCT_CREATION_IN_INVENTORY_RESPONSE)
                .partitions(numPartitions)
                .replicas(numReplicas)
                .build();
    }
}

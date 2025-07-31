package com.microservice.inventory.service.kafka;

import java.util.HashMap;
import java.util.Map;

public class KafkaResponseBuilder {

    private final Map<String, Object> response;

    private KafkaResponseBuilder() {
        this.response = new HashMap<>();
    }

    public static KafkaResponseBuilder create() {
        return new KafkaResponseBuilder();
    }

    public KafkaResponseBuilder status(String status) {
        response.put("status", status);
        return this;
    }

    public KafkaResponseBuilder data(Object data) {
        response.put("data", data);
        return this;
    }

    public Map<String, Object> build() {
        return response;
    }
}

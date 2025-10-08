package com.commerce.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {
    private final KafkaTemplate<String, InventoryEvent> template;
    private final String topic;

    public InventoryEventPublisher(
            KafkaTemplate<String, InventoryEvent> template,
            @Value("${inventory.events.topic:inventory-events}") String topic) {
        this.template = template;
        this.topic = topic;
    }

    public void publish(InventoryEvent evt) {
        template.send(topic, evt.getProductId().toString(), evt);
    }
}

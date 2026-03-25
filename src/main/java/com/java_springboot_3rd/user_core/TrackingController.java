package com.java_springboot_3rd.user_core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnProperty(name = "app.features.kafka-tracking", havingValue = "true")
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_NAME = "game_analytics_topic";

    @PostMapping("/event")
    public ResponseEntity<String> recordEvent(@RequestBody TrackingEvent event) {
        // đẩy vào Kafka mà không cần chờ
        kafkaTemplate.send(TOPIC_NAME, event);

        // Trả về HTTP 202 (Accepted)
        return new ResponseEntity<>("Ghi nhận Tracking Event thành công!", HttpStatus.ACCEPTED);
    }
}
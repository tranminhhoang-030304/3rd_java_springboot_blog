package com.java_springboot_3rd.user_core;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsWorker {

    // Kẹp dấu trang (offset) và trực trên "game_analytics_topic"
    @KafkaListener(topics = "game_analytics_topic", groupId = "data-analytics-group")
    public void consumeTrackingEvent(TrackingEvent event) {
        System.out.println(" [KAFKA] Ghi nhận sự kiện mới:");
        System.out.println("   User: " + event.getUserId());
        System.out.println("   Hành động: " + event.getAction() + " (Qua " + event.getPlatform() + ")");
        System.out.println("   Thời gian: " + event.getTimestamp());
        System.out.println("   => Đã lưu log vào hệ thống phân tích Big Data!");
        System.out.println("---------------------------------------------------");
    }
}
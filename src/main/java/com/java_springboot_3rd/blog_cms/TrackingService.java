package com.java_springboot_3rd.blog_cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class TrackingService {

    //OPTIONAL: Báo cho Spring biết "Có thì dùng, không có thì thôi, đừng báo lỗi sập server"
    @Autowired
    private Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    public void logPostView(Integer postId, String postTitle) {
        if (kafkaTemplate.isPresent()) {
            // Nếu công tắc Kafka đang bật -> Bắn sự kiện
            String trackingEvent = " Có người vừa xem bài viết ID " + postId + " - Tiêu đề: " + postTitle;
            kafkaTemplate.get().send("blog_tracking_topic", trackingEvent);
            System.out.println(" [Kafka] Đã ghi nhận sự kiện đọc bài viết!");
        } else {
            // Nếu công tắc tắt -> Im lặng hoặc in log cảnh báo nhẹ nhàng
            System.out.println(" [Tracking] Kafka đang tắt, bỏ qua ghi nhận view.");
        }
    }
}
package com.java_springboot_3rd.blog_cms;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class NotificationService {

    // OPTIONAL
    @Autowired
    private Optional<RabbitTemplate> rabbitTemplate;

    public void sendCommentNotification(String author, String postTitle) {
        if (rabbitTemplate.isPresent()) {
            // Nếu RabbitMQ bật
            String notification = "Bài viết '" + postTitle + "' vừa có bình luận mới từ " + author + "!";
            // Lưu ý: Nếu có file Config chứa EXCHANGE_NAME thì thay vào
            rabbitTemplate.get().convertAndSend("ecommerce_exchange", "blog.comment.key", notification);
            System.out.println("[RabbitMQ] Đã đẩy sự kiện thông báo bình luận vào Queue!");
        } else {
            // Nếu RabbitMQ tắt
            System.out.println("[Notification] RabbitMQ đang tắt, không gửi thông báo.");
        }
    }
}
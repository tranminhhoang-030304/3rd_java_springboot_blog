package com.java_springboot_3rd.user_core;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "ecommerce_order_queue"; // 1. Tên của Hàng đợi (Hòm thư)
    public static final String EXCHANGE_NAME = "ecommerce_exchange"; // 2. Tên của Tổng đài phân loại
    public static final String ROUTING_KEY = "order.routing.key";   // 3. Mã bưu điện (Routing key) để Tổng đài biết ném thư vào Hòm nào

    // --- BẮT ĐẦU XÂY DỰNG ---
    @Bean
    public Queue myQueue() {
        // Tạo một Hàng đợi bền vững (true = sập Server, khởi động lại vẫn còn)
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange myExchange() {
        // Tạo lớp phân loại thư theo kiểu Direct (Chỉ định đích danh mã bưu điện)
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue myQueue, DirectExchange myExchange) {
        // Nối lớp phân loại với Hòm thư thông qua Mã
        return BindingBuilder.bind(myQueue).to(myExchange).with(ROUTING_KEY);
    }

    // Yêu cầu RabbitMQ cách đóng gói Object Java thành JSON trước khi gửi đi
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
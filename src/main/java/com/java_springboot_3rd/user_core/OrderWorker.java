package com.java_springboot_3rd.user_core;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component //khai báo
@ConditionalOnProperty(name = "app.features.rabbitmq-notify", havingValue = "true")
public class OrderWorker {

    /**
    // Trực Queue
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processOrder(String message) {
        System.out.println(" Có tin nhắn từ Queue: " + message);
        System.out.println(" Tạo hóa đơn PDF và gửi Email... (Mất 5s)");

        try {
            Thread.sleep(5000); // Giả lập việc gửi email chậm chạp mất 5 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(" Đã gửi Email cho đơn hàng: " + message);
    }
    */

    // trực queue, có JSON truyền đến là đưa thành Object OrderEvent
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processRealOrder(OrderEvent order) {
        System.out.println("");
        System.out.println("Quý khách có HÀNG mới!");
        System.out.println(" Mã đơn hàng : " + order.getOrderId());
        System.out.println(" Sản phẩm    : " + order.getProductName());
        System.out.println(" Thanh toán  : $" + order.getPrice());
        System.out.println(" Đang tạo PDF và gửi Email tới: " + order.getUserEmail() + " ... (Mất 5s)");

        try {
            Thread.sleep(5000); // Giả lập việc tạo PDF và gửi Email
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Đã gửi Email hóa đơn cho đơn " + order.getOrderId() + "!");
        System.out.println("\n");
    }
}
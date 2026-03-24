package com.java_springboot_3rd.user_core;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DailyReportScheduler {

    // 1.TEST: Chạy lặp đi lặp lại mỗi 10 giây (10000 mili-giây)
    @Scheduled(fixedRate = 10000)
    public void runTestJob() {
        System.out.println("[SCHEDULER] Hệ thống vừa tự động quét hệ thống lúc: " + LocalDateTime.now());
    }

    // 2. Schedule: Chạy đúng 12h đêm hàng ngày (Dùng cấu trúc Cron: Giây - Phút - Giờ - Ngày - Tháng - Thứ)
    @Scheduled(cron = "0 0 0 * * ?")
    public void runMidnightReport() {
        System.out.println("[SCHEDULER] Đã đến lịch! Đang ......");
        // Gọi UserService, OrderRepository dưới đây để cron chạy theo công việc yêu cầu
    }
}
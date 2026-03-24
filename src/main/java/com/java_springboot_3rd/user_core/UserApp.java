package com.java_springboot_3rd.user_core;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

//@Component // Đánh dấu đây là một mảnh ghép của hệ thống
public class UserApp implements CommandLineRunner {

    @Autowired // Kêu gọi Spring thêm UserService vào đây
    private UserService userService;

    // Hàm này sẽ tự động chạy ngay sau khi Spring Boot khởi động xong
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 [System] Ứng dụng Spring Boot đã khởi động thành công!");
        System.out.println("👤 [Người dùng] Click nút Đăng ký với tên: java_master");

        // Gọi hàm đăng ký
        //userService.registerUser("sping_booter");
    }
}
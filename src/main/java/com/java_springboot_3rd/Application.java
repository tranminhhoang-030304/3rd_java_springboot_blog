package com.java_springboot_3rd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    /**
     http://localhost:8081/swagger-ui/index.html
     http://localhost:9090/targets
     http://localhost:15672/#/
     http://localhost:3000
     */
}

package com.ticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ticket.user.mapper") // 兜底，确保 Mapper 被扫描到

public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("server started");
    }
}

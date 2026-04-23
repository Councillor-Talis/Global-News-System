package com.gns;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gns.mapper")
public class GlobalNewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlobalNewsApplication.class, args);
        System.out.println("启动成功 → http://localhost:8080");
    }
}

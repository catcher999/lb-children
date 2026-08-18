package com.platform.lbchildren;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.platform.lbchildren.mapper")
@EnableScheduling
public class LbChildrenApplication {
    public static void main(String[] args) {
        SpringApplication.run(LbChildrenApplication.class, args);
    }
}
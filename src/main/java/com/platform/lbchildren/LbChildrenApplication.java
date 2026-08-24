package com.platform.lbchildren;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// 本工程采用 JWT 无状态认证，不使用 Spring Security 默认用户体系，
// 排除 UserDetailsServiceAutoConfiguration 以避免生成默认密码的启动警告。
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan("com.platform.lbchildren.domain.mapper")
@EnableScheduling
public class LbChildrenApplication {
    public static void main(String[] args) {
        SpringApplication.run(LbChildrenApplication.class, args);
    }
}
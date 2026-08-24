package com.platform.lbchildren.config;

import com.platform.lbchildren.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开：登录注册、H2 控制台、WebSocket 端点
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()   // 上传的图片等静态资源
                        .requestMatchers("/api/parent/register", "/api/parent/login",
                                "/api/child/login").permitAll()
                        // 教育资源列表公开，进度接口需登录
                        .requestMatchers("/api/resources", "/api/resources/types").permitAll()
                        // 儿童：日记、相册
                        .requestMatchers("/api/child/add-diary", "/api/child/upload-album",
                                "/api/child/my-diaries", "/api/child/my-albums").hasRole("CHILD")
                        // 公共登录（儿童+家长）
                        .requestMatchers("/api/child/**", "/api/resources/**").hasAnyRole("CHILD", "PARENT")
                        .requestMatchers("/api/parent/**").hasRole("PARENT")
                        .requestMatchers("/api/treehole/**").hasAnyRole("PARENT", "CHILD")
                        .requestMatchers("/api/ai/**").hasAnyRole("PARENT", "CHILD")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
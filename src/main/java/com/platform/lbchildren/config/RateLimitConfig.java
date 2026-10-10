package com.platform.lbchildren.config;

import com.platform.lbchildren.common.ratelimit.InMemorySlidingWindowRateLimiter;
import com.platform.lbchildren.common.ratelimit.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 防滥用限流配置（B3）
 * <p>
 * 三道闸，只限行为特征、阈值远高于人类正常使用；阈值可通过 application yaml 调整或整体关闭：
 * - globalRateLimiter : 全局硬闸（防单机被打爆，不分用户）
 * - userRateLimiter   : 按用户滑动窗口（防单账号高频刷漏）
 * - dupQuestionRateLimiter : 同用户同句去重（防脚本同句批量刷）
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiter globalRateLimiter(
            @Value("${ai.ratelimit.global-window-secs:1}") int windowSecs,
            @Value("${ai.ratelimit.global-max:500}") int max) {
        return new InMemorySlidingWindowRateLimiter(windowSecs, max);
    }

    @Bean
    public RateLimiter userRateLimiter(
            @Value("${ai.ratelimit.user-window-secs:60}") int windowSecs,
            @Value("${ai.ratelimit.user-max:20}") int max) {
        return new InMemorySlidingWindowRateLimiter(windowSecs, max);
    }

    @Bean
    public RateLimiter dupQuestionRateLimiter(
            @Value("${ai.ratelimit.dup-window-secs:10}") int windowSecs,
            @Value("${ai.ratelimit.dup-max:5}") int max) {
        return new InMemorySlidingWindowRateLimiter(windowSecs, max);
    }
}
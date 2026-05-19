package com.platform.lbchildren.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application-secret.yaml", ignoreResourceNotFound = true)
public class SecretConfig {

    @Value("${jwt.secret:placeholder-jwt-secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${ai.api.key:placeholder-api-key}")
    private String aiApiKey;

    @Value("${ai.api.url:https://api.deepseek.com/chat/completions}")
    private String aiApiUrl;

    @Value("${ai.model:deepseek-chat}")
    private String aiModel;

    @Value("${ai.child.daily-limit:15}")
    private Integer aiChildDailyLimit;

    // Getters
    public String getJwtSecret() {
        return jwtSecret;
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public String getAiApiKey() {
        return aiApiKey;
    }

    public String getAiApiUrl() {
        return aiApiUrl;
    }

    public String getAiModel() {
        return aiModel;
    }

    public Integer getAiChildDailyLimit() {
        return aiChildDailyLimit;
    }
}


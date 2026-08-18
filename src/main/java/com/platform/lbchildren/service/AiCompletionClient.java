package com.platform.lbchildren.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 文本补全客户端（OpenAI 兼容格式）
 * <p>
 * 供 AI 聊天、记忆画像压缩（阶段三）、后续 RAG 检索注入等复用。
 * 显式按 UTF-8 解码响应，避免 RestTemplate 默认 ISO-8859-1 导致中文乱码。
 */
@Slf4j
@Component
public class AiCompletionClient {

    private final RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    public AiCompletionClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 调用 DeepSeek，返回模型生成文本。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户输入
     * @return 模型生成的文本
     * @throws BusinessException 调用失败时抛出（ResultCode.AI_SERVICE_ERROR）
     */
    public String complete(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.postForEntity(apiUrl, new HttpEntity<>(requestBody, headers), byte[].class);
        } catch (Exception e) {
            log.error("DeepSeek 调用失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }

        try {
            byte[] raw = response.getBody();
            String body = new String(raw, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(body, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek 响应解析失败", e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }
    }
}

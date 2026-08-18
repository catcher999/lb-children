package com.platform.lbchildren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.dto.AIChatResponse;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.mapper.AIChatHistoryMapper;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.AIChatService;
import com.platform.lbchildren.service.MemoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 聊天业务实现（DeepSeek API）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final RestTemplate restTemplate;
    private final AIChatHistoryMapper historyMapper;
    private final MemoryService memoryService;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.child.daily-limit:15}")
    private int childDailyLimit;

    /** 敏感词库（启动时从 sensitive-words.txt 加载） */
    private final Set<String> sensitiveWords = new HashSet<>();

    /**
     * 加载 classpath 下的敏感词文件；加载失败时回退到内置词库
     */
    @PostConstruct
    public void initSensitiveWords() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("sensitive-words.txt").getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(sensitiveWords::add);
            log.info("敏感词库加载完成，共 {} 个词", sensitiveWords.size());
        } catch (IOException e) {
            log.warn("敏感词文件加载失败，使用内置词库", e);
            sensitiveWords.addAll(List.of("暴力", "自杀", "色情"));
        }
    }

    @Override
    public AIChatResponse ask(UserPrincipal user, String question) {
        // 1. 内容安全过滤
        if (containsSensitive(question)) {
            throw new BusinessException(ResultCode.CONTENT_SENSITIVE);
        }

        // 2. 频率限制（针对儿童）
        if ("CHILD".equalsIgnoreCase(user.getRole())) {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            Long count = historyMapper.selectCount(new LambdaQueryWrapper<AIChatHistory>()
                    .eq(AIChatHistory::getUserId, user.getUserId())
                    .gt(AIChatHistory::getCreatedAt, todayStart));
            if (count >= childDailyLimit) {
                throw new BusinessException(ResultCode.AI_LIMIT_EXCEEDED);
            }
        }

        // 3. 构建 system prompt（保证回答适合儿童），并注入记忆「近况」（冷启动时为空，回退固定 prompt）
        String systemPrompt = "你是一个为留守儿童和外出务工家长提供心理支持与教育辅导的AI助手。" +
                "你的回答应温暖、鼓励、积极，适合6-16岁儿童阅读，避免负面或敏感内容。";
        String context = memoryService.getContext(user);
        if (!context.isEmpty()) {
            systemPrompt = systemPrompt + "\n" + context;
        }

        // 4. 调用大模型 API（OpenAI 兼容格式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", question));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.postForEntity(apiUrl, entity, byte[].class);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }

        // 5. 解析返回结果（显式按 UTF-8 解码，避免 RestTemplate 默认 ISO-8859-1 导致中文乱码）
        String answer;
        try {
            byte[] raw = response.getBody();
            log.info("DeepSeek raw body length={}, first bytes={}, content-type={}",
                    raw == null ? -1 : raw.length,
                    raw == null ? "null" : java.util.HexFormat.of().formatHex(
                            raw.length >= 8 ? java.util.Arrays.copyOf(raw, 8) : raw),
                    response.getHeaders().getContentType());
            String body = new String(raw, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(body, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            answer = (String) message.get("content");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }

        // 6. 保存历史
        AIChatHistory history = new AIChatHistory();
        history.setUserId(user.getUserId());
        history.setUserRole(user.getRole());
        history.setQuestion(question);
        history.setAnswer(answer);
        history.setCreatedAt(LocalDateTime.now());
        historyMapper.insert(history);

        // 7. 返回
        AIChatResponse resp = new AIChatResponse();
        resp.setAnswer(answer);
        resp.setHistoryId(history.getId());
        return resp;
    }

    @Override
    public List<AIChatHistory> getHistory(Long userId) {
        return historyMapper.selectList(new LambdaQueryWrapper<AIChatHistory>()
                .eq(AIChatHistory::getUserId, userId)
                .orderByDesc(AIChatHistory::getCreatedAt));
    }

    private boolean containsSensitive(String text) {
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}

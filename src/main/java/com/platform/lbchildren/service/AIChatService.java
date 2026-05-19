package com.platform.lbchildren.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.lbchildren.dto.AIChatResponse;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.repository.AIChatHistoryRepository;
import com.platform.lbchildren.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AIChatService {

    private final RestTemplate restTemplate;
    private final AIChatHistoryRepository historyRepository;

    // 配置在 application.yml 中
    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.child.daily.limit:20}")   // 儿童每日限制
    private int childDailyLimit;

    // 敏感词列表（简单示例，实际应从文件或数据库加载）
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(
            Arrays.asList("暴力", "自杀", "色情")); // 请替换为真实词库

    public AIChatService(RestTemplate restTemplate,
                         AIChatHistoryRepository historyRepository) {
        this.restTemplate = restTemplate;
        this.historyRepository = historyRepository;
    }

    public AIChatResponse ask(UserPrincipal user, String question) throws Exception {
        // 1. 内容安全过滤
        if (containsSensitive(question)) {
            throw new RuntimeException("您的问题包含不适当内容，请修改后重试。");
        }

        // 2. 频率限制（针对儿童）
        if ("CHILD".equalsIgnoreCase(user.getRole())) {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            long count = historyRepository.countByUserIdAndCreatedAtAfter(user.getUserId(), todayStart);
            if (count >= childDailyLimit) {
                throw new RuntimeException("今日提问次数已达上限，请明天再来问 AI 小助手吧~");
            }
        }

        // 3. 构建 system prompt（保证回答适合儿童）
        String systemPrompt = "你是一个为留守儿童和外出务工家长提供心理支持与教育辅导的AI助手。" +
                "你的回答应温暖、鼓励、积极，适合6-16岁儿童阅读，避免负面或敏感内容。";

        // 4. 调用大模型 API（以 OpenAI 兼容格式为例）
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
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

        // 5. 解析返回结果
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI 没有返回有效回答");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String answer = (String) message.get("content");

        // 6. 保存历史
        AIChatHistory history = new AIChatHistory();
        history.setUserId(user.getUserId());
        history.setUserRole(user.getRole());
        history.setQuestion(question);
        history.setAnswer(answer);
        historyRepository.save(history);

        // 7. 返回
        AIChatResponse resp = new AIChatResponse();
        resp.setAnswer(answer);
        resp.setHistoryId(history.getId());
        return resp;
    }

    public List<AIChatHistory> getHistory(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private boolean containsSensitive(String text) {
        for (String word : SENSITIVE_WORDS) {
            if (text.contains(word)) return true;
        }
        return false;
    }
}
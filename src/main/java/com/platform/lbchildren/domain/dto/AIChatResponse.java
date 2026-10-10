package com.platform.lbchildren.domain.dto;

import lombok.Data;

@Data
public class AIChatResponse {
    private String answer;
    private Long historyId;   // 可选，用于记录
    private Long sessionId;   // 本次回答所属会话 ID（B1）
}
package com.platform.lbchildren.dto;

import lombok.Data;

@Data
public class AIChatResponse {
    private String answer;
    private Long historyId;   // 可选，用于记录
}
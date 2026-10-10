package com.platform.lbchildren.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 会话列表视图（B1）
 */
@Data
public class AiSessionVO {
    private Long id;
    private String title;
    private String lastPreview;   // 最近一条提问，用于列表预览
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
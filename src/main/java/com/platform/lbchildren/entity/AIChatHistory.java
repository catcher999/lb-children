package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 聊天历史实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_chat_history")
public class AIChatHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提问者 ID */
    private Long userId;

    /** PARENT 或 CHILD */
    private String userRole;

    private String question;

    private String answer;

    private LocalDateTime createdAt;
}

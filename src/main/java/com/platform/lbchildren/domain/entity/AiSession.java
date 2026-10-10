package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 会话实体（B1：会话建模，承载多轮上下文）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_session")
public class AiSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话所属用户 ID */
    private Long userId;

    /** PARENT 或 CHILD */
    private String userRole;

    /** 会话标题（取首问截断） */
    private String title;

    /** 会话内更早内容压缩摘要（增量压缩产物，仅作背景事实注入） */
    private String summary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
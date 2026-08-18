package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 长期画像实体（阶段三：LLM 压缩出的核心记忆）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 记忆主体 ID */
    private Long userId;

    /** PARENT 或 CHILD */
    private String userRole;

    /** LLM 压缩出的长期画像 */
    private String profileSummary;

    private LocalDateTime updatedAt;
}

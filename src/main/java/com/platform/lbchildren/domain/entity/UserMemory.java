package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 短期记忆实体（阶段二：AI 记忆）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_memory")
public class UserMemory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 记忆主体 ID */
    private Long userId;

    /** PARENT 或 CHILD */
    private String userRole;

    /** 数据来源：DIARY/CHAT/ALBUM/TREEHOLE/MARKED */
    private String category;

    /** 记忆条目内容（已清洗） */
    private String content;

    /** 情感标签：HAPPY/SAD/ANGRY/ANXIOUS/NONE */
    private String emotion;

    /** 初始重要性 */
    private Double importance;

    /** 最近被引用时间（引用强化重置） */
    private LocalDateTime lastAccessed;

    /** 升级路径 L1/L2/L3 */
    private String level;

    /** active/archived */
    private String status;

    /** 溯源原记录 id */
    private Long sourceId;

    private LocalDateTime createdAt;
}

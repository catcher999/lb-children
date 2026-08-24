package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 学习进度实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("learning_progress")
public class LearningProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long resourceId;

    private Integer progressPercent;

    private LocalDateTime lastLearnTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

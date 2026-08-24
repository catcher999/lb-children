package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 权威文献实体（阶段四 RAG 通道：只放权威心理/教育/安全指南，绝不存用户个人数据）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("literature")
public class Literature {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文献/指南标题 */
    private String title;

    /** 发布机构 */
    private String source;

    /** 来源链接 */
    private String sourceUrl;

    /** 分类：PSYCHOLOGY/EDUCATION/SAFETY/CRISIS/USE_DIGITAL */
    private String category;

    /** 适用对象：CHILD/PARENT/BOTH */
    private String audience;

    /** 整理后的要点正文（供注入） */
    private String summary;

    /** 检索关键词（空格分隔） */
    private String keywords;

    private LocalDateTime createdAt;
}

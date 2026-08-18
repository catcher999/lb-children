package com.platform.lbchildren.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 孩子画像摘要视图对象（阶段五：家长授权后可查看）
 */
@Data
public class ChildProfileVO {

    private Long childId;

    private String nickname;

    /** LLM 压缩出的长期画像摘要 */
    private String profileSummary;

    /** 画像更新时间 */
    private LocalDateTime updatedAt;
}

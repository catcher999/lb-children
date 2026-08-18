package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 树洞帖子实体
 * 内部记录发布者身份（authorUserId / authorRole），对外响应不暴露
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("treehole_post")
public class TreeholePost {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String imageUrl;

    /** 内部记录发布者 */
    private Long authorUserId;

    /** PARENT 或 CHILD */
    private String authorRole;

    private LocalDateTime createdAt;
}

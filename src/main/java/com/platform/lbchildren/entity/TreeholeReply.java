package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 树洞回复实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("treehole_reply")
public class TreeholeReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String imageUrl;

    /** 内部记录回复者 */
    private Long authorUserId;

    private String authorRole;

    /** 关联帖子 ID */
    private Long postId;

    private LocalDateTime createdAt;
}

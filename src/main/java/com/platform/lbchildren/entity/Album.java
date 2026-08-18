package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 成长相册实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("album")
public class Album {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String imageUrl;

    private String description;

    /** 关联儿童 ID */
    private Long childId;

    private LocalDateTime createdAt;
}

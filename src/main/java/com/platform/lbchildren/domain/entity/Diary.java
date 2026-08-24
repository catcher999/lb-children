package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 儿童日记实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("diary")
public class Diary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String imageUrl;

    /** 是否匿名 */
    private Boolean isAnonymous = false;

    /** 关联儿童 ID */
    private Long childId;

    private LocalDateTime createdAt;
}

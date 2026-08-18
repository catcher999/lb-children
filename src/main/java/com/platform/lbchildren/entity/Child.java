package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 儿童信息实体（通过 parentId 关联家长）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("child")
public class Child {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;

    private Integer age;

    private String avatar;

    /** 关联家长 ID */
    private Long parentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

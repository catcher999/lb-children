package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RBAC 角色实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，如 PARENT / CHILD / ADMIN */
    private String code;

    private String name;

    private String description;

    private LocalDateTime createdAt;
}

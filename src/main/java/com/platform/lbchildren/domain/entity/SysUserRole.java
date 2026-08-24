package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RBAC 用户-角色关联实体
 * <p>
 * 用 user_id + user_role 复合定位用户（parent 与 child 的 id 可能重复），
 * user_role 指数据源类型（PARENT/CHILD）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 家长或儿童的用户 ID */
    private Long userId;

    /** 数据源类型 PARENT/CHILD */
    private String userRole;

    private Long roleId;

    private LocalDateTime createdAt;
}

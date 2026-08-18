package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 家长用户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("parent")
public class Parent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 加密后的密码 */
    private String password;

    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

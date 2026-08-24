package com.platform.lbchildren.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 家长登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

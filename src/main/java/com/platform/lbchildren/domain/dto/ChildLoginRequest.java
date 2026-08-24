package com.platform.lbchildren.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 儿童登录请求：家长用户名 + 家长密码 + 儿童昵称
 */
@Data
public class ChildLoginRequest {

    @NotBlank(message = "家长用户名不能为空")
    private String parentUsername;

    @NotBlank(message = "家长密码不能为空")
    private String parentPassword;

    @NotBlank(message = "儿童昵称不能为空")
    private String childNickname;
}

package com.platform.lbchildren.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 添加儿童请求
 */
@Data
public class AddChildRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @NotNull(message = "年龄不能为空")
    private Integer age;
}

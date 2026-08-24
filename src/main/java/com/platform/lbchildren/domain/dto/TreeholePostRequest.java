package com.platform.lbchildren.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 树洞发帖请求
 */
@Data
public class TreeholePostRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容长度不能超过2000")
    private String content;

    private String imageUrl;
}

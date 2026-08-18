package com.platform.lbchildren.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 树洞回复请求
 */
@Data
public class TreeholeReplyRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容长度不能超过2000")
    private String content;

    private String imageUrl;
}

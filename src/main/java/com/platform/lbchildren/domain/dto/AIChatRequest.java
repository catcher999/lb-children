package com.platform.lbchildren.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 聊天请求
 */
@Data
public class AIChatRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500")
    private String question;
}

package com.platform.lbchildren.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 儿童日记请求
 */
@Data
public class DiaryRequest {

    @NotBlank(message = "日记内容不能为空")
    private String content;

    private String imageUrl;

    private Boolean isAnonymous;
}

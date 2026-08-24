package com.platform.lbchildren.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 家长授权查看孩子画像请求（阶段五）
 */
@Data
public class ProfileConsentRequest {

    /** true=授权，false=撤销 */
    @NotNull(message = "consent 不能为空")
    private Boolean consent;
}

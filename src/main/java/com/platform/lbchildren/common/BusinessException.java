package com.platform.lbchildren.common;

import lombok.Getter;

/**
 * 业务异常：Service 层抛出，由 GlobalExceptionHandler 统一捕获
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}

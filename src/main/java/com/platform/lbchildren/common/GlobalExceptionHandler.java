package com.platform.lbchildren.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：统一拦截业务异常 / 参数校验异常 / 系统异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @Valid 请求体校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.PARAM_ERROR.getMsg();
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /** 表单绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.PARAM_ERROR.getMsg();
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}

package com.platform.lbchildren.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    USERNAME_EXIST(1001, "用户名已存在"),
    LOGIN_FAIL(1002, "用户名或密码错误"),
    PARENT_NOT_FOUND(1003, "家长账号不存在"),
    CHILD_NOT_FOUND(1004, "儿童不存在"),
    AI_LIMIT_EXCEEDED(1005, "今日提问次数已达上限"),
    CONTENT_SENSITIVE(1006, "内容包含不适当信息"),
    NOT_FAMILY(1007, "只能与自己关联的家长或儿童聊天"),
    PROFILE_NOT_CONSENTED(1008, "家长未授权查看孩子画像"),

    DATABASE_ERROR(5001, "数据库操作失败"),
    AI_SERVICE_ERROR(5002, "AI 服务暂时不可用"),
    SYSTEM_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String msg;
}

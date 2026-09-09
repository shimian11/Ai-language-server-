package com.ailang.common.result;

import lombok.Getter;

/**
 * 统一响应码。前端 request.ts 约定：code=0 成功；401 触发跳转登录。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "ok"),
    BAD_REQUEST(400, "请求参数或业务规则错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

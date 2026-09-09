package com.ailang.common.exception;

import com.ailang.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：message 必须为用户可读中文，由全局异常处理器转为 ApiResponse。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}

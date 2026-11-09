package com.ailang.common.exception;

import com.ailang.common.result.ApiResponse;
import com.ailang.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：任何异常都以统一 JSON 格式返回，绝不输出堆栈给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, message);
    }

    /** @RequestParam / @PathVariable 上的校验注解（如 @NotBlank）触发 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 已下线接口 / 不存在路径：按 404 语义返回，不记 ERROR 日志 */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ApiResponse<Void> handleNotFound(org.springframework.web.servlet.resource.NoResourceFoundException exception) {
        return ApiResponse.fail(ResultCode.NOT_FOUND, "接口不存在");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception exception) {
        log.error("未处理异常", exception);
        return ApiResponse.fail(ResultCode.SERVER_ERROR, ResultCode.SERVER_ERROR.getMessage());
    }
}

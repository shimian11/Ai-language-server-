package com.ailang.common.result;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * M0 启动验收用探针：GET /api/ping 返回统一响应体格式。
 */
@RestController
public class PingController {

    @GetMapping("/api/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.ok("pong");
    }
}

package com.ailang.service.auth.controller;

import com.ailang.common.exception.BizException;
import com.ailang.common.result.ApiResponse;
import com.ailang.common.security.JwtUtil;
import com.ailang.service.auth.dto.LoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口 #1：登录。账号密码来自 .env（ADMIN_USERNAME / ADMIN_PASSWORD）。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    /** 管理员账号（.env ADMIN_USERNAME） */
    @Value("${ailang.admin.username:}")
    private String adminUsername;

    /** 管理员密码（.env ADMIN_PASSWORD） */
    @Value("${ailang.admin.password:}")
    private String adminPassword;

    /**
     * #1 管理员登录：核对账密后签发 JWT。
     *
     * @param request 登录请求（username / password 必填）
     * @return 成功时 data 为 JWT 字符串；账密错误时返回 code=400
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest request) {
        boolean ok = adminUsername != null && !adminUsername.isBlank()
                && adminUsername.equals(request.getUsername())
                && adminPassword != null && adminPassword.equals(request.getPassword());
        if (!ok) {
            throw new BizException("用户名或密码错误");
        }
        return ApiResponse.ok(jwtUtil.generate());
    }
}

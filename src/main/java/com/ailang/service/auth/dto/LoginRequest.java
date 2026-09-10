package com.ailang.service.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员登录请求。
 */
@Getter
@Setter
public class LoginRequest {

    /** 管理员用户名（与 .env 的 ADMIN_USERNAME 比对） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 管理员密码（与 .env 的 ADMIN_PASSWORD 比对） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
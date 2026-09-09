package com.ailang.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 启动时自检关键配置占位是否已被环境变量解析（只记录是否非空，绝不打印敏感值）。
 */
@Slf4j
@Configuration
public class EnvCheckRunner {

    @Value("${ailang.jwt.secret:}")
    private String jwtSecret;

    @Value("${ailang.admin.username:}")
    private String adminUsername;

    @Value("${ailang.admin.password:}")
    private String adminPassword;

    @Value("${ailang.minio.endpoint:}")
    private String minioEndpoint;

    @PostConstruct
    public void check() {
        checkPlaceholder("JWT_SECRET", jwtSecret);
        checkPlaceholder("ADMIN_USERNAME", adminUsername);
        checkPlaceholder("ADMIN_PASSWORD", adminPassword);
        checkPlaceholder("MINIO_ENDPOINT", minioEndpoint);
    }

    private void checkPlaceholder(String name, String value) {
        if (value == null || value.isBlank()) {
            log.warn("配置自检：{} 未注入（请检查 .env / IDEA EnvFile / compose env_file）", name);
        } else {
            log.info("配置自检：{} 已注入", name);
        }
    }
}

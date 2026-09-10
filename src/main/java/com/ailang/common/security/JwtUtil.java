package com.ailang.common.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 签发与校验。载荷 {sub:"admin", exp}，有效期 ailang.jwt.expire-days（默认 7 天）。
 */
@Component
public class JwtUtil {

    @Value("${ailang.jwt.secret:}")
    private String secret;

    @Value("${ailang.jwt.expire-days:7}")
    private int expireDays;

    private SecretKey key;

    @PostConstruct
    void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 未配置或长度不足 32 字符（HS256 要求）");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发管理员 token */
    public String generate() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("admin")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireDays, ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    /** 校验 token，有效返回 subject；无效/过期返回 null（拒绝策略由安全层统一处理） */
    public String verify(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}

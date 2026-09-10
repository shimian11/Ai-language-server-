package com.ailang.common.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "ssh.tunnel")
@ConditionalOnProperty(name = "ssh.tunnel.enabled", havingValue = "true")
public class SshConfig {

    private String host;
    private int port = 22;
    private String username;
    private String password;
    private String privateKey;
    private List<Forward> forwards;

    private Session session;

    @Data
    public static class Forward {
        private int localPort;
        private String remoteHost;
        private int remotePort;
    }

    @PostConstruct
    public void init() {
        try {
            JSch jsch = new JSch();
            String keyPath = (privateKey == null || privateKey.isBlank())
                    ? System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"
                    : privateKey;
            boolean keyExists = new File(keyPath).exists();
            if (keyExists) {
                jsch.addIdentity(keyPath);
                log.info("SSH认证方式: 私钥 {}", keyPath);
            } else if (password != null && !password.isBlank()) {
                log.info("SSH认证方式: 密码（私钥 {} 不存在）", keyPath);
            } else {
                throw new IllegalStateException("SSH私钥不存在且未配置密码: " + keyPath);
            }
            session = jsch.getSession(username, host, port);
            if (password != null && !password.isBlank()) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15000);
            for (Forward forward : forwards) {
                int assignedPort = session.setPortForwardingL(
                        forward.getLocalPort(),
                        forward.getRemoteHost(),
                        forward.getRemotePort()
                );
                log.info("SSH隧道已建立: localhost:{} -> {}:{}", assignedPort, forward.getRemoteHost(), forward.getRemotePort());
            }
        } catch (Exception e) {
            log.error("SSH隧道建立失败", e);
            throw new RuntimeException("SSH隧道建立失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH隧道已断开");
        }
    }
}

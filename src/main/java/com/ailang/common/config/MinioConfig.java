package com.ailang.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端。endpoint 为浏览器可达的公网地址（预签名 URL 的域名即由此生成）。
 * 桶由 compose 的 create-bucket 服务建，这里启动时兜底校验/创建（幂等）。
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Value("${ailang.minio.endpoint:}")
    private String endpoint;

    @Value("${ailang.minio.access-key:}")
    private String accessKey;

    @Value("${ailang.minio.secret-key:}")
    private String secretKey;

    @Value("${ailang.minio.bucket:ailang-images}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO 桶已创建: {}", bucket);
            } else {
                log.info("MinIO 桶已存在: {}", bucket);
            }
        } catch (Exception e) {
            // 预签名为本地签名计算、不依赖网络连接；建桶失败不阻断启动（生产由 create-bucket 服务负责）
            log.warn("MinIO 桶检查/创建失败（不影响预签名生成）: {}", e.getMessage());
        }
        return client;
    }
}

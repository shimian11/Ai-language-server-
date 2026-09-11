package com.ailang.service.upload.service.impl;

import com.ailang.common.exception.BizException;
import com.ailang.service.upload.dto.PresignVO;
import com.ailang.service.upload.service.UploadService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 预签名直传实现。SIGV4 签名为本地计算，不与 MinIO 建立连接；
 * 文件体由浏览器直接 PUT 到 uploadUrl，不经后端中转。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final int EXPIRY_MINUTES = 10;

    private final MinioClient minioClient;

    @Value("${ailang.minio.bucket:ailang-images}")
    private String bucket;

    @Value("${ailang.minio.endpoint:}")
    private String endpoint;

    /** 生成预签名上传 URL 与最终对象访问 URL（SIGV4 本地签名，不建连接） */
    @Override
    public PresignVO presign(String fileName) {
        String objectName = buildObjectName(fileName);
        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(objectName)
                    .expiry(EXPIRY_MINUTES, TimeUnit.MINUTES)
                    .build());
            return new PresignVO(uploadUrl, objectUrl(objectName));
        } catch (Exception e) {
            log.error("生成预签名 URL 失败: objectName={}", objectName, e);
            throw new BizException("生成上传链接失败，请稍后重试");
        }
    }

    /**
     * 对象名 images/yyyy/MM/uuid.ext：UUID 主名防冲突与非法字符问题，仅保留安全扩展名。
     */
    private String buildObjectName(String fileName) {
        String ext = "";
        if (fileName != null) {
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0 && dot < fileName.length() - 1) {
                String candidate = fileName.substring(dot + 1).toLowerCase();
                if (candidate.matches("[a-z0-9]{1,10}")) {
                    ext = "." + candidate;
                }
            }
        }
        LocalDate now = LocalDate.now();
        return "images/" + now.getYear() + "/" + String.format("%02d", now.getMonthValue())
                + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    /** 拼接对象的最终公开访问地址（endpoint/bucket/objectName） */
    private String objectUrl(String objectName) {
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return base + "/" + bucket + "/" + objectName;
    }
}

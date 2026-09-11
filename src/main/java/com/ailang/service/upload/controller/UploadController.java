package com.ailang.service.upload.controller;

import com.ailang.common.result.ApiResponse;
import com.ailang.service.upload.dto.PresignVO;
import com.ailang.service.upload.service.UploadService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 上传接口 #14（需鉴权，由 SecurityConfig 兜底拦截）。
 */
@Validated
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * #14 预签名直传：入参 fileName，返回 {uploadUrl, objectUrl}。
     *
     * @param fileName 待上传文件名（必填）
     * @return 预签名上传信息（uploadUrl 用于浏览器直传，objectUrl 为最终访问地址）
     */
    @GetMapping("/presign")
    public ApiResponse<PresignVO> presign(@RequestParam @NotBlank(message = "文件名不能为空") String fileName) {
        return ApiResponse.ok(uploadService.presign(fileName));
    }
}

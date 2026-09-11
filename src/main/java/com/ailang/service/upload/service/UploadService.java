package com.ailang.service.upload.service;

import com.ailang.service.upload.dto.PresignVO;

/**
 * 上传 Service：负责生成预签名直传所需的 URL。
 */
public interface UploadService {

    /** #14 预签名直传：PUT 用 uploadUrl + 读取用 objectUrl，有效期 10 分钟 */
    PresignVO presign(String fileName);
}

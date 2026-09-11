package com.ailang.service.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 预签名直传结果（#14，与前端 upload.ts 的 PresignResult 对齐）。
 */
@Data
@AllArgsConstructor
public class PresignVO {

    /** 浏览器 PUT 直传用（含签名参数，10 分钟有效） */
    private String uploadUrl;

    /** 上传成功后的公网读取地址（随案例入库存储） */
    private String objectUrl;
}

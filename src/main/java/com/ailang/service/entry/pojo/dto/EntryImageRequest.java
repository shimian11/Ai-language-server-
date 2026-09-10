package com.ailang.service.entry.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图片保存项（顺序即 sort）。
 */
@Data
public class EntryImageRequest {

    /** 图片对象 URL */
    @NotBlank(message = "图片地址不能为空")
    private String url;

    /** 是否主图 */
    private Boolean isMain;
}
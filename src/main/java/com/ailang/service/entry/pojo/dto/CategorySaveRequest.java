package com.ailang.service.entry.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增/重命名分类请求。
 */
@Data
public class CategorySaveRequest {
    /** 分类名称（唯一） */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过 50 字")
    private String name;
}
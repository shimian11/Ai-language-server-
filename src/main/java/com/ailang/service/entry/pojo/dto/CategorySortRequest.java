package com.ailang.service.entry.pojo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量排序请求：ids 顺序即 sort 顺序（1..n）。
 */
@Data
public class CategorySortRequest {

    /** 排序后的分类 ID 列表（顺序即 sort 1..n） */
    @NotEmpty(message = "排序列表不能为空")
    private List<Long> ids;
}